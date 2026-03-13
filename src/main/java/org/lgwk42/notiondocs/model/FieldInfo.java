package org.lgwk42.notiondocs.model;

/**
 * Metadata of a single DTO field.
 *
 * @param name     field name (e.g. "email")
 * @param type     type string (e.g. "String", "List&lt;Item&gt;")
 * @param required whether the field is required (determined by @NotNull, @NotBlank, etc.)
 * @param children nested fields if this is an object type
 */
public record FieldInfo(
        String name,
        String type,
        boolean required,
        java.util.List<FieldInfo> children
) {
    public FieldInfo(String name, String type, boolean required) {
        this(name, type, required, java.util.List.of());
    }
}
