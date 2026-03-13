package io.github.lgwk42.notiondocs.autoconfigure;

import io.github.lgwk42.notiondocs.listener.NotionDocsSyncListener;
import io.github.lgwk42.notiondocs.notion.client.NotionApiClient;
import io.github.lgwk42.notiondocs.notion.render.FieldTableBuilder;
import io.github.lgwk42.notiondocs.notion.render.JsonExampleGenerator;
import io.github.lgwk42.notiondocs.notion.render.NotionDatabaseRenderer;
import io.github.lgwk42.notiondocs.notion.render.NotionPageRenderer;
import io.github.lgwk42.notiondocs.scanner.ApiEndpointScanner;
import io.github.lgwk42.notiondocs.scanner.DtoFieldExtractor;
import io.github.lgwk42.notiondocs.scanner.EndpointMetadataResolver;
import io.github.lgwk42.notiondocs.scanner.EndpointParameterExtractor;
import io.github.lgwk42.notiondocs.sync.ChangeDetector;
import io.github.lgwk42.notiondocs.sync.DocumentSyncService;
import io.github.lgwk42.notiondocs.sync.NotionDatabaseManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Auto-configuration for Notion API documentation generation.
 * Activated when notion-docs.enabled=true (default) and the application is a web application.
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "notion-docs", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(NotionDocsProperties.class)
public class NotionDocsAutoConfiguration {

    @Bean
    public DtoFieldExtractor dtoFieldExtractor() {
        return new DtoFieldExtractor();
    }

    @Bean
    public EndpointMetadataResolver endpointMetadataResolver(NotionDocsProperties properties) {
        return new EndpointMetadataResolver(
                properties.getDefaultDomain(),
                properties.getDefaultAuth()
        );
    }

    @Bean
    public EndpointParameterExtractor endpointParameterExtractor() {
        return new EndpointParameterExtractor();
    }

    @Bean
    public ApiEndpointScanner apiEndpointScanner(RequestMappingHandlerMapping handlerMapping,
                                                  DtoFieldExtractor fieldExtractor,
                                                  EndpointMetadataResolver metadataResolver,
                                                  EndpointParameterExtractor parameterExtractor,
                                                  NotionDocsProperties properties) {
        return new ApiEndpointScanner(
                handlerMapping,
                fieldExtractor,
                metadataResolver,
                parameterExtractor,
                properties.getIncludePackages(),
                properties.getExcludePackages()
        );
    }

    @Bean
    public NotionApiClient notionApiClient(NotionDocsProperties properties) {
        properties.validate();
        return new NotionApiClient(
                properties.getApiToken(),
                properties.getNotionApiVersion(),
                properties.getConnectTimeout()
        );
    }

    @Bean
    public NotionDatabaseRenderer notionDatabaseRenderer() {
        return new NotionDatabaseRenderer();
    }

    @Bean
    public JsonExampleGenerator jsonExampleGenerator() {
        return new JsonExampleGenerator();
    }

    @Bean
    public FieldTableBuilder fieldTableBuilder() {
        return new FieldTableBuilder();
    }

    @Bean
    public NotionPageRenderer notionPageRenderer(JsonExampleGenerator jsonExampleGenerator,
                                                  FieldTableBuilder fieldTableBuilder) {
        return new NotionPageRenderer(jsonExampleGenerator, fieldTableBuilder);
    }

    @Bean
    public ChangeDetector changeDetector() {
        return new ChangeDetector();
    }

    @Bean
    public NotionDatabaseManager notionDatabaseManager(NotionApiClient notionClient,
                                                        NotionDatabaseRenderer databaseRenderer) {
        return new NotionDatabaseManager(notionClient, databaseRenderer);
    }

    @Bean
    public DocumentSyncService documentSyncService(ApiEndpointScanner scanner,
                                                    NotionApiClient notionClient,
                                                    NotionDatabaseRenderer databaseRenderer,
                                                    NotionPageRenderer pageRenderer,
                                                    ChangeDetector changeDetector,
                                                    NotionDatabaseManager databaseManager,
                                                    NotionDocsProperties properties) {
        return new DocumentSyncService(
                scanner,
                notionClient,
                databaseRenderer,
                pageRenderer,
                changeDetector,
                databaseManager,
                properties.getParentPageId(),
                properties.getDatabaseTitle(),
                properties.isArchiveRemovedEndpoints()
        );
    }

    @Bean
    public NotionDocsSyncListener notionDocsSyncListener(DocumentSyncService syncService,
                                                          NotionDocsProperties properties) {
        return new NotionDocsSyncListener(syncService, properties.isSyncOnStartup());
    }
}
