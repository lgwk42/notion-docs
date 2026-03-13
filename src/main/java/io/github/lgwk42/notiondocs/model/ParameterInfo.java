package io.github.lgwk42.notiondocs.model;

/**
 * Metadata of a request parameter (path variable or query parameter).
 *
 * @param name         parameter name (e.g. "id", "page")
 * @param type         type string (e.g. "Long", "String")
 * @param required     whether the parameter is required
 * @param defaultValue default value (null if none)
 * @param paramType    PATH_VARIABLE or QUERY_PARAMETER
 */
public record ParameterInfo(
        String name,
        String type,
        boolean required,
        String defaultValue,
        ParamType paramType
) {
    public enum ParamType {
        PATH_VARIABLE,
        QUERY_PARAMETER
    }
}
