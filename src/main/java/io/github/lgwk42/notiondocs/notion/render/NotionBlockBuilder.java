package io.github.lgwk42.notiondocs.notion.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating Notion Block JSON structures.
 */
public class NotionBlockBuilder {

    private NotionBlockBuilder() {
    }

    public static Map<String, Object> heading1(String text) {
        return Map.of(
                "object", "block",
                "type", "heading_1",
                "heading_1", Map.of("rich_text", richText(text))
        );
    }

    public static Map<String, Object> heading2(String text) {
        return Map.of(
                "object", "block",
                "type", "heading_2",
                "heading_2", Map.of("rich_text", richText(text))
        );
    }

    public static Map<String, Object> heading3(String text) {
        return Map.of(
                "object", "block",
                "type", "heading_3",
                "heading_3", Map.of("rich_text", richText(text))
        );
    }

    public static Map<String, Object> paragraph(String text) {
        return Map.of(
                "object", "block",
                "type", "paragraph",
                "paragraph", Map.of("rich_text", richText(text))
        );
    }

    public static Map<String, Object> emptyParagraph() {
        return Map.of(
                "object", "block",
                "type", "paragraph",
                "paragraph", Map.of("rich_text", List.of())
        );
    }

    public static Map<String, Object> boldParagraph(String text) {
        return Map.of(
                "object", "block",
                "type", "paragraph",
                "paragraph", Map.of("rich_text", List.of(
                        Map.of("type", "text",
                                "text", Map.of("content", text),
                                "annotations", Map.of("bold", true))
                ))
        );
    }

    public static Map<String, Object> codeBlock(String code, String language) {
        return Map.of(
                "object", "block",
                "type", "code",
                "code", Map.of(
                        "rich_text", richText(code),
                        "language", language
                )
        );
    }

    public static Map<String, Object> callout(String text, String emoji) {
        Map<String, Object> callout = new LinkedHashMap<>();
        callout.put("rich_text", richText(text));
        callout.put("icon", Map.of("type", "emoji", "emoji", emoji));
        return Map.of(
                "object", "block",
                "type", "callout",
                "callout", callout
        );
    }

    public static Map<String, Object> divider() {
        return Map.of(
                "object", "block",
                "type", "divider",
                "divider", Map.of()
        );
    }

    /**
     * Creates a Notion table block with headers and data rows.
     */
    public static Map<String, Object> table(List<String> headers, List<List<String>> rows) {
        int width = headers.size();
        List<Map<String, Object>> tableRows = new ArrayList<>();
        tableRows.add(tableRow(headers));
        for (List<String> row : rows) {
            tableRows.add(tableRow(row));
        }
        Map<String, Object> tableContent = new LinkedHashMap<>();
        tableContent.put("table_width", width);
        tableContent.put("has_column_header", true);
        tableContent.put("has_row_header", false);
        tableContent.put("children", tableRows);
        return Map.of(
                "object", "block",
                "type", "table",
                "table", tableContent
        );
    }

    private static Map<String, Object> tableRow(List<String> cells) {
        List<List<Map<String, Object>>> cellContents = cells.stream()
                .map(NotionBlockBuilder::richText)
                .toList();
        return Map.of(
                "type", "table_row",
                "table_row", Map.of("cells", cellContents)
        );
    }

    public static List<Map<String, Object>> richText(String text) {
        return List.of(Map.of(
                "type", "text",
                "text", Map.of("content", text)
        ));
    }

    public static Map<String, Object> titleProperty(String text) {
        return Map.of("title", richText(text));
    }

    public static Map<String, Object> richTextProperty(String text) {
        return Map.of("rich_text", richText(text));
    }

    public static Map<String, Object> selectProperty(String value) {
        return Map.of("select", Map.of("name", value));
    }

    public static Map<String, Object> multiSelectProperty(List<String> values) {
        List<Map<String, String>> options = values.stream()
                .map(v -> Map.of("name", v))
                .toList();
        return Map.of("multi_select", options);
    }
}
