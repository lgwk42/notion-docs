package org.lgwk42.notiondocs.listener;

import org.lgwk42.notiondocs.sync.DocumentSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Triggers Notion documentation sync on ApplicationReadyEvent.
 * Runs on a virtual thread to avoid blocking application startup.
 */
public class NotionDocsSyncListener {

    private static final Logger log = LoggerFactory.getLogger(NotionDocsSyncListener.class);

    private final DocumentSyncService syncService;
    private final boolean syncOnStartup;

    public NotionDocsSyncListener(DocumentSyncService syncService, boolean syncOnStartup) {
        this.syncService = syncService;
        this.syncOnStartup = syncOnStartup;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!syncOnStartup) {
            log.info("Notion documentation sync is disabled (sync-on-startup=false).");
            return;
        }
        Thread.ofVirtual().name("notion-docs-sync").start(() -> {
            try {
                DocumentSyncService.SyncResult result = syncService.syncAll();
                log.info("Notion documentation sync complete: {} created, {} updated, {} skipped, {} archived.",
                        result.created(), result.updated(), result.skipped(), result.archived());
            } catch (Exception e) {
                log.error("Notion documentation sync failed.", e);
            }
        });
    }
}
