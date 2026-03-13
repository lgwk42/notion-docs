package org.lgwk42.notiondocs.sync;

import com.fasterxml.jackson.databind.JsonNode;
import org.lgwk42.notiondocs.notion.client.NotionApiClient;
import org.lgwk42.notiondocs.notion.render.NotionDatabaseRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages Notion Database operations: creation, querying, and block manipulation.
 */
public class NotionDatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(NotionDatabaseManager.class);

    private final NotionApiClient notionClient;
    private final NotionDatabaseRenderer databaseRenderer;

    public NotionDatabaseManager(NotionApiClient notionClient,
                                  NotionDatabaseRenderer databaseRenderer) {
        this.notionClient = notionClient;
        this.databaseRenderer = databaseRenderer;
    }

    /**
     * Finds an existing database under the parent page or creates a new one.
     */
    public String findOrCreateDatabase(String parentPageId, String databaseTitle) {
        JsonNode children = notionClient.getBlockChildren(parentPageId);
        if (children.has("results")) {
            for (JsonNode block : children.get("results")) {
                if ("child_database".equals(block.path("type").asText())) {
                    return block.get("id").asText();
                }
            }
        }
        log.info("Creating new database: {}", databaseTitle);
        Map<String, Object> schema = databaseRenderer.createDatabaseSchema();
        JsonNode db = notionClient.createDatabase(parentPageId, databaseTitle, schema);
        return db.get("id").asText();
    }

    /**
     * Queries existing rows and returns a map of "METHOD URI" to ExistingRow.
     */
    public Map<String, ExistingRow> queryExistingRows(String databaseId) {
        Map<String, ExistingRow> rows = new LinkedHashMap<>();
        JsonNode result = notionClient.queryDatabase(databaseId, null);
        if (result.has("results")) {
            for (JsonNode page : result.get("results")) {
                String pageId = page.get("id").asText();
                JsonNode properties = page.get("properties");
                String uri = extractRichText(properties.path("URI"));
                String method = extractSelectText(properties.path("METHOD"));
                String syncHash = extractRichText(properties.path("_sync_hash"));
                if (uri != null && method != null) {
                    String key = method + " " + uri;
                    rows.put(key, new ExistingRow(pageId, syncHash));
                }
            }
        }
        return rows;
    }

    /**
     * Deletes all block children of a page.
     */
    public void deleteAllBlockChildren(String pageId) {
        JsonNode children = notionClient.getBlockChildren(pageId);
        if (children.has("results")) {
            for (JsonNode block : children.get("results")) {
                notionClient.deleteBlock(block.get("id").asText());
            }
        }
    }

    /**
     * Appends blocks in chunks of 100 to respect Notion API limits.
     */
    public void appendBlocksInChunks(String pageId, List<Map<String, Object>> blocks) {
        int chunkSize = 100;
        for (int i = 0; i < blocks.size(); i += chunkSize) {
            List<Map<String, Object>> chunk = blocks.subList(i, Math.min(i + chunkSize, blocks.size()));
            notionClient.appendBlockChildren(pageId, chunk);
        }
    }

    private String extractSelectText(JsonNode selectProp) {
        return selectProp.path("select").path("name").asText(null);
    }

    private String extractRichText(JsonNode richTextProp) {
        JsonNode arr = richTextProp.path("rich_text");
        if (arr.isArray() && !arr.isEmpty()) {
            return arr.get(0).path("text").path("content").asText(null);
        }
        return null;
    }

    /**
     * Represents an existing database row.
     */
    public record ExistingRow(String pageId, String syncHash) {
    }
}
