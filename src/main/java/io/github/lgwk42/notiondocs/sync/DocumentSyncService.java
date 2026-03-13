package io.github.lgwk42.notiondocs.sync;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.lgwk42.notiondocs.model.ApiEndpointInfo;
import io.github.lgwk42.notiondocs.model.ControllerGroup;
import io.github.lgwk42.notiondocs.notion.client.NotionApiClient;
import io.github.lgwk42.notiondocs.notion.render.NotionDatabaseRenderer;
import io.github.lgwk42.notiondocs.notion.render.NotionPageRenderer;
import io.github.lgwk42.notiondocs.scanner.ApiEndpointScanner;
import io.github.lgwk42.notiondocs.sync.NotionDatabaseManager.ExistingRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Orchestrates the API documentation sync flow: scan > compare > sync to Notion.
 * Delegates database management to {@link NotionDatabaseManager}.
 */
public class DocumentSyncService {

    private static final Logger log = LoggerFactory.getLogger(DocumentSyncService.class);

    private final ApiEndpointScanner scanner;
    private final NotionApiClient notionClient;
    private final NotionDatabaseRenderer databaseRenderer;
    private final NotionPageRenderer pageRenderer;
    private final ChangeDetector changeDetector;
    private final NotionDatabaseManager databaseManager;
    private final String parentPageId;
    private final String databaseTitle;
    private final boolean archiveRemoved;

    public DocumentSyncService(ApiEndpointScanner scanner,
                               NotionApiClient notionClient,
                               NotionDatabaseRenderer databaseRenderer,
                               NotionPageRenderer pageRenderer,
                               ChangeDetector changeDetector,
                               NotionDatabaseManager databaseManager,
                               String parentPageId,
                               String databaseTitle,
                               boolean archiveRemoved) {
        this.scanner = scanner;
        this.notionClient = notionClient;
        this.databaseRenderer = databaseRenderer;
        this.pageRenderer = pageRenderer;
        this.changeDetector = changeDetector;
        this.databaseManager = databaseManager;
        this.parentPageId = parentPageId;
        this.databaseTitle = databaseTitle;
        this.archiveRemoved = archiveRemoved;
    }

    /**
     * Executes the full synchronization.
     */
    public SyncResult syncAll() {
        log.info("Starting API documentation sync...");
        List<ControllerGroup> groups = scanner.scan();
        int totalEndpoints = groups.stream()
                .mapToInt(g -> g.endpoints().size())
                .sum();
        log.info("Found {} endpoints across {} controllers.", totalEndpoints, groups.size());
        SyncResult result = syncAsDatabase(groups);
        log.info("Sync complete: {} created, {} updated, {} skipped, {} archived.",
                result.created(), result.updated(), result.skipped(), result.archived());
        return result;
    }

    private SyncResult syncAsDatabase(List<ControllerGroup> groups) {
        int created = 0, updated = 0, skipped = 0, archived = 0;
        String databaseId = databaseManager.findOrCreateDatabase(parentPageId, databaseTitle);
        Map<String, ExistingRow> existingRows = databaseManager.queryExistingRows(databaseId);
        Set<String> currentKeys = new HashSet<>();
        for (ControllerGroup group : groups) {
            for (ApiEndpointInfo endpoint : group.endpoints()) {
                String key = endpoint.httpMethod() + " " + endpoint.uri();
                currentKeys.add(key);
                String newHash = changeDetector.computeHash(endpoint);
                ExistingRow existing = existingRows.get(key);
                if (existing != null) {
                    if (changeDetector.hasChanged(existing.syncHash(), newHash)) {
                        Map<String, Object> properties = databaseRenderer.renderRowForUpdate(endpoint, newHash);
                        notionClient.updatePage(existing.pageId(), properties);
                        databaseManager.deleteAllBlockChildren(existing.pageId());
                        List<Map<String, Object>> blocks = pageRenderer.renderEndpointPageBody(endpoint);
                        databaseManager.appendBlocksInChunks(existing.pageId(), blocks);
                        updated++;
                        log.debug("Updated: {}", key);
                    } else {
                        skipped++;
                        log.debug("Skipped (unchanged): {}", key);
                    }
                } else {
                    Map<String, Object> properties = databaseRenderer.renderRow(endpoint, newHash);
                    JsonNode page = notionClient.createDatabaseRow(databaseId, properties);
                    String newPageId = page.get("id").asText();
                    List<Map<String, Object>> blocks = pageRenderer.renderEndpointPageBody(endpoint);
                    databaseManager.appendBlocksInChunks(newPageId, blocks);
                    created++;
                    log.debug("Created: {}", key);
                }
            }
        }
        if (archiveRemoved) {
            for (Map.Entry<String, ExistingRow> entry : existingRows.entrySet()) {
                if (!currentKeys.contains(entry.getKey())) {
                    notionClient.archivePage(entry.getValue().pageId());
                    archived++;
                    log.debug("Archived: {}", entry.getKey());
                }
            }
        }
        return new SyncResult(created, updated, skipped, archived);
    }

    public record SyncResult(int created, int updated, int skipped, int archived) {
    }
}
