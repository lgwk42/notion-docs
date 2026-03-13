package io.github.lgwk42.notiondocs.model;

import java.util.List;

/**
 * Complete metadata of a single API endpoint.
 *
 * @param name            API display name (e.g. "Sign In")
 * @param description     API description
 * @param domain          domain category (e.g. "AUTH", "MEETING")
 * @param authRoles       access roles (e.g. ["ALL"], ["USER", "ADMIN"])
 * @param uri             API URI (e.g. "/api/v1/users/{id}")
 * @param httpMethod      HTTP method (GET, POST, PUT, DELETE, etc.)
 * @param headers         request headers
 * @param pathVariables   path variables
 * @param queryParams     query parameters
 * @param requestBody     request body fields (empty list if none)
 * @param requestBodyType request body type name (null if none)
 * @param responseFields  response body fields (empty list if none)
 * @param responseType    response type name (null if void)
 * @param controllerName  controller class name
 * @param methodName      handler method name
 */
public record ApiEndpointInfo(
        String name,
        String description,
        String domain,
        List<String> authRoles,
        String uri,
        String httpMethod,
        List<HeaderInfo> headers,
        List<ParameterInfo> pathVariables,
        List<ParameterInfo> queryParams,
        List<FieldInfo> requestBody,
        String requestBodyType,
        List<FieldInfo> responseFields,
        String responseType,
        String controllerName,
        String methodName
) {
}
