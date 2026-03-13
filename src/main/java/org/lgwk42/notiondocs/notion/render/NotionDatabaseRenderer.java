package org.lgwk42.notiondocs.notion.render;

import org.lgwk42.notiondocs.model.ApiEndpointInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.lgwk42.notiondocs.notion.render.NotionBlockBuilder.multiSelectProperty;
import static org.lgwk42.notiondocs.notion.render.NotionBlockBuilder.richTextProperty;
import static org.lgwk42.notiondocs.notion.render.NotionBlockBuilder.selectProperty;
import static org.lgwk42.notiondocs.notion.render.NotionBlockBuilder.titleProperty;

/**
 * Renders API endpoints as Notion Database row properties.
 *
 * <p>Schema: API(title), URI(rich_text), METHOD(select), AUTH(multi_select),
 * DOMAIN(select), PROGRESS(select), _sync_hash(rich_text)</p>
 */
public class NotionDatabaseRenderer {

    /**
     * Creates the database schema (property definitions).
     */
    public Map<String, Object> createDatabaseSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("API", Map.of("title", Map.of()));
        schema.put("URI", Map.of("rich_text", Map.of()));
        schema.put("METHOD", Map.of("select", Map.of(
                "options", List.of(
                        Map.of("name", "GET", "color", "green"),
                        Map.of("name", "POST", "color", "blue"),
                        Map.of("name", "PUT", "color", "yellow"),
                        Map.of("name", "PATCH", "color", "orange"),
                        Map.of("name", "DELETE", "color", "red")
                )
        )));
        schema.put("AUTH", Map.of("multi_select", Map.of()));
        schema.put("DOMAIN", Map.of("select", Map.of()));
        schema.put("PROGRESS", Map.of("select", Map.of(
                "options", List.of(
                        Map.of("name", "Not Started", "color", "default"),
                        Map.of("name", "In Progress", "color", "yellow"),
                        Map.of("name", "Done", "color", "green")
                )
        )));
        schema.put("_sync_hash", Map.of("rich_text", Map.of()));
        return schema;
    }

    /**
     * Converts an endpoint to database row properties for new row creation.
     * PROGRESS defaults to "Not Started".
     */
    public Map<String, Object> renderRow(ApiEndpointInfo endpoint, String syncHash) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("API", titleProperty(endpoint.name()));
        properties.put("URI", richTextProperty(endpoint.uri()));
        properties.put("METHOD", selectProperty(endpoint.httpMethod()));
        if (!endpoint.authRoles().isEmpty()) {
            properties.put("AUTH", multiSelectProperty(endpoint.authRoles()));
        }
        if (endpoint.domain() != null && !endpoint.domain().isEmpty()) {
            properties.put("DOMAIN", selectProperty(endpoint.domain()));
        }
        properties.put("PROGRESS", selectProperty("Not Started"));
        properties.put("_sync_hash", richTextProperty(syncHash));
        return properties;
    }

    /**
     * Converts an endpoint to database row properties for update.
     * PROGRESS is excluded to preserve the user-managed value.
     */
    public Map<String, Object> renderRowForUpdate(ApiEndpointInfo endpoint, String syncHash) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("API", titleProperty(endpoint.name()));
        properties.put("URI", richTextProperty(endpoint.uri()));
        properties.put("METHOD", selectProperty(endpoint.httpMethod()));
        if (!endpoint.authRoles().isEmpty()) {
            properties.put("AUTH", multiSelectProperty(endpoint.authRoles()));
        }
        if (endpoint.domain() != null && !endpoint.domain().isEmpty()) {
            properties.put("DOMAIN", selectProperty(endpoint.domain()));
        }
        properties.put("_sync_hash", richTextProperty(syncHash));
        return properties;
    }
}
