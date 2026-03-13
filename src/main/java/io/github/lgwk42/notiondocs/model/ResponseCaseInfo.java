package io.github.lgwk42.notiondocs.model;

import java.util.List;

/**
 * Metadata of a single response case for an API endpoint.
 *
 * @param status         HTTP status code (e.g. 200, 404, 500)
 * @param description    description of this response case
 * @param responseType   response body type name (null if no body)
 * @param responseFields response body fields (empty list if no body)
 */
public record ResponseCaseInfo(
        int status,
        String description,
        String responseType,
        List<FieldInfo> responseFields
) {
}
