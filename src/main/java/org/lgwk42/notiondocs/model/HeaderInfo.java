package org.lgwk42.notiondocs.model;

/**
 * Metadata of a single request header.
 *
 * @param name         header name (e.g. "Authorization")
 * @param type         type string (e.g. "String")
 * @param required     whether the header is required
 * @param defaultValue default value (null if none)
 */
public record HeaderInfo(
        String name,
        String type,
        boolean required,
        String defaultValue
) {
}
