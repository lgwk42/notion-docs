package io.github.lgwk42.notiondocs.notion.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.lgwk42.notiondocs.notion.exception.NotionApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Notion REST API client.
 * Uses JDK HttpClient to operate without Spring dependencies.
 */
public class NotionApiClient {

    private static final Logger log = LoggerFactory.getLogger(NotionApiClient.class);
    private static final String BASE_URL = "https://api.notion.com/v1";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiToken;
    private final String notionVersion;

    public NotionApiClient(String apiToken, String notionVersion,
                           Duration connectTimeout) {
        this.apiToken = apiToken;
        this.notionVersion = notionVersion != null ? notionVersion : "2022-06-28";
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout != null ? connectTimeout : Duration.ofSeconds(5))
                .build();
    }

    /**
     * Creates a page under the given parent page.
     */
    public JsonNode createPage(String parentPageId, String title, List<Map<String, Object>> children) {
        ObjectNode body = objectMapper.createObjectNode();
        ObjectNode parent = body.putObject("parent");
        parent.put("page_id", parentPageId);
        ObjectNode properties = body.putObject("properties");
        ObjectNode titleProp = properties.putObject("title");
        ArrayNode titleArray = titleProp.putArray("title");
        ObjectNode titleText = titleArray.addObject();
        titleText.putObject("text").put("content", title);
        if (children != null && !children.isEmpty()) {
            body.set("children", objectMapper.valueToTree(children));
        }
        return post("/pages", body);
    }

    /**
     * Updates a page's properties.
     */
    public JsonNode updatePage(String pageId, Map<String, Object> properties) {
        ObjectNode body = objectMapper.createObjectNode();
        body.set("properties", objectMapper.valueToTree(properties));
        return patch("/pages/" + pageId, body);
    }

    /**
     * Archives a page.
     */
    public JsonNode archivePage(String pageId) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("archived", true);
        return patch("/pages/" + pageId, body);
    }

    /**
     * Appends block children to a page or block.
     */
    public JsonNode appendBlockChildren(String blockId, List<Map<String, Object>> children) {
        ObjectNode body = objectMapper.createObjectNode();
        body.set("children", objectMapper.valueToTree(children));
        return patch("/blocks/" + blockId + "/children", body);
    }

    /**
     * Retrieves block children of a page or block.
     */
    public JsonNode getBlockChildren(String blockId) {
        return get("/blocks/" + blockId + "/children");
    }

    /**
     * Creates a database under the given parent page.
     */
    public JsonNode createDatabase(String parentPageId, String title,
                                    Map<String, Object> propertiesSchema) {
        ObjectNode body = objectMapper.createObjectNode();
        ObjectNode parent = body.putObject("parent");
        parent.put("page_id", parentPageId);
        ArrayNode titleArray = body.putArray("title");
        ObjectNode titleText = titleArray.addObject();
        titleText.putObject("text").put("content", title);
        body.set("properties", objectMapper.valueToTree(propertiesSchema));
        return post("/databases", body);
    }

    /**
     * Queries a database with an optional filter.
     */
    public JsonNode queryDatabase(String databaseId, Map<String, Object> filter) {
        ObjectNode body = objectMapper.createObjectNode();
        if (filter != null && !filter.isEmpty()) {
            body.set("filter", objectMapper.valueToTree(filter));
        }
        return post("/databases/" + databaseId + "/query", body);
    }

    /**
     * Creates a row (page) in a database.
     */
    public JsonNode createDatabaseRow(String databaseId, Map<String, Object> properties) {
        ObjectNode body = objectMapper.createObjectNode();
        ObjectNode parent = body.putObject("parent");
        parent.put("database_id", databaseId);
        body.set("properties", objectMapper.valueToTree(properties));
        return post("/pages", body);
    }

    /**
     * Retrieves a page by ID.
     */
    public JsonNode getPage(String pageId) {
        return get("/pages/" + pageId);
    }

    /**
     * Deletes a block by ID.
     */
    public JsonNode deleteBlock(String blockId) {
        return delete("/blocks/" + blockId);
    }

    private JsonNode get(String path) {
        HttpRequest request = newRequestBuilder(path).GET().build();
        return execute(request);
    }

    private JsonNode post(String path, ObjectNode body) {
        HttpRequest request = newRequestBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(toJson(body)))
                .build();
        return execute(request);
    }

    private JsonNode patch(String path, ObjectNode body) {
        HttpRequest request = newRequestBuilder(path)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(toJson(body)))
                .build();
        return execute(request);
    }

    private JsonNode delete(String path) {
        HttpRequest request = newRequestBuilder(path).DELETE().build();
        return execute(request);
    }

    private HttpRequest.Builder newRequestBuilder(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + apiToken)
                .header("Notion-Version", notionVersion)
                .header("Content-Type", "application/json");
    }

    private JsonNode execute(HttpRequest request) {
        try {
            log.debug("Notion API {} {}", request.method(), request.uri());
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode responseBody = objectMapper.readTree(response.body());
            if (response.statusCode() >= 400) {
                String errorCode = responseBody.has("code") ? responseBody.get("code").asText() : "unknown";
                String message = responseBody.has("message") ? responseBody.get("message").asText() : response.body();
                throw new NotionApiException(
                        "Notion API error: " + message,
                        response.statusCode(),
                        errorCode
                );
            }
            return responseBody;
        } catch (NotionApiException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new NotionApiException("Failed to call Notion API: " + e.getMessage(), e);
        }
    }

    private String toJson(ObjectNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new NotionApiException("Failed to serialize request body", e);
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
