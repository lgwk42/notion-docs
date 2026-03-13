package io.github.lgwk42.notiondocs.model;

import java.util.List;

/**
 * A group of endpoints belonging to a single controller.
 *
 * @param controllerName controller class name (e.g. "UserController")
 * @param basePath       class-level @RequestMapping path (e.g. "/api/v1/users")
 * @param endpoints      endpoints in this controller
 */
public record ControllerGroup(
        String controllerName,
        String basePath,
        List<ApiEndpointInfo> endpoints
) {
}
