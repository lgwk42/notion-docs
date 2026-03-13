package io.github.lgwk42.notiondocs.notion.render;

import io.github.lgwk42.notiondocs.model.FieldInfo;

import java.util.List;

/**
 * Generates JSON example strings from FieldInfo lists.
 */
public class JsonExampleGenerator {

    /**
     * Generates a JSON example from the given fields.
     */
    public String generate(List<FieldInfo> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        appendFields(fields, sb, 1);
        sb.append("}");
        return sb.toString();
    }

    private void appendFields(List<FieldInfo> fields, StringBuilder sb, int depth) {
        String indent = "  ".repeat(depth);
        for (int i = 0; i < fields.size(); i++) {
            FieldInfo field = fields.get(i);
            sb.append(indent).append("\"").append(field.name()).append("\": ");
            if (!field.children().isEmpty()) {
                if (isArrayOrListType(field.type())) {
                    sb.append("[\n");
                    sb.append(indent).append("  {\n");
                    appendFields(field.children(), sb, depth + 2);
                    sb.append(indent).append("  }\n");
                    sb.append(indent).append("]");
                } else {
                    sb.append("{\n");
                    appendFields(field.children(), sb, depth + 1);
                    sb.append(indent).append("}");
                }
            } else {
                sb.append(getExampleValue(field.type()));
            }
            if (i < fields.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
    }

    private String getExampleValue(String type) {
        return switch (type.toLowerCase()) {
            case "string" -> "\"string\"";
            case "int", "integer", "long" -> "0";
            case "double", "float" -> "0.0";
            case "boolean" -> "true";
            case "localdate" -> "\"2026-01-01\"";
            case "localdatetime" -> "\"2026-01-01T00:00:00\"";
            case "list", "array" -> "[]";
            default -> "\"string\"";
        };
    }

    private boolean isArrayOrListType(String type) {
        String lower = type.toLowerCase();
        return lower.contains("list") || lower.contains("array") || lower.contains("set")
                || lower.contains("collection");
    }
}
