package org.lgwk42.notiondocs.notion.render;

import org.lgwk42.notiondocs.model.FieldInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lgwk42.notiondocs.notion.render.NotionBlockBuilder.table;

/**
 * Builds a Notion table block from FieldInfo list using dot notation for nested fields.
 */
public class FieldTableBuilder {

    /**
     * Creates a Parameter table block from the given fields.
     * Nested fields use dot notation (parent.child), arrays use [] notation (list[].item).
     */
    public Map<String, Object> build(List<FieldInfo> fields) {
        List<List<String>> rows = flattenFields(fields, "");
        return table(
                List.of("Parameter", "Type", "Description"),
                rows
        );
    }

    private List<List<String>> flattenFields(List<FieldInfo> fields, String prefix) {
        List<List<String>> rows = new ArrayList<>();
        for (FieldInfo field : fields) {
            String name = prefix.isEmpty() ? field.name() : prefix + "." + field.name();
            String desc = field.required() ? "(required)" : "(optional)";
            rows.add(List.of(name, field.type(), desc));
            if (!field.children().isEmpty()) {
                String childPrefix = isArrayOrListType(field.type()) ? name + "[]" : name;
                rows.addAll(flattenFields(field.children(), childPrefix));
            }
        }
        return rows;
    }

    private boolean isArrayOrListType(String type) {
        String lower = type.toLowerCase();
        return lower.contains("list") || lower.contains("array") || lower.contains("set")
                || lower.contains("collection");
    }
}
