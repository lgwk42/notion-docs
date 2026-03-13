package io.github.lgwk42.notiondocs.scanner;

import io.github.lgwk42.notiondocs.annotation.NotionDoc;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Resolves API metadata (name, description, domain, auth) from {@link NotionDoc} annotations.
 */
public class EndpointMetadataResolver {

    private final String defaultDomain;
    private final List<String> defaultAuth;

    public EndpointMetadataResolver(String defaultDomain, List<String> defaultAuth) {
        this.defaultDomain = defaultDomain != null ? defaultDomain : "";
        this.defaultAuth = defaultAuth != null ? defaultAuth : List.of();
    }

    /**
     * Resolves the API display name.
     * Priority: method @NotionDoc.name > method name.
     */
    public String resolveApiName(Method method, Class<?> controllerClass) {
        NotionDoc methodDoc = method.getAnnotation(NotionDoc.class);
        if (methodDoc != null && !methodDoc.name().isEmpty()) {
            return methodDoc.name();
        }
        return method.getName();
    }

    /**
     * Resolves the API description.
     * Priority: method @NotionDoc.description > empty string.
     */
    public String resolveDescription(Method method, Class<?> controllerClass) {
        NotionDoc methodDoc = method.getAnnotation(NotionDoc.class);
        if (methodDoc != null && !methodDoc.description().isEmpty()) {
            return methodDoc.description();
        }
        return "";
    }

    /**
     * Resolves the domain category.
     * Priority: method @NotionDoc.domain > class @NotionDoc.domain > inferred from controller name > defaultDomain.
     */
    public String resolveDomain(Method method, Class<?> controllerClass, String controllerName) {
        NotionDoc methodDoc = method.getAnnotation(NotionDoc.class);
        if (methodDoc != null && !methodDoc.domain().isEmpty()) {
            return methodDoc.domain();
        }
        NotionDoc classDoc = controllerClass.getAnnotation(NotionDoc.class);
        if (classDoc != null && !classDoc.domain().isEmpty()) {
            return classDoc.domain();
        }
        String inferred = inferDomainFromControllerName(controllerName);
        if (!inferred.isEmpty()) {
            return inferred;
        }
        return defaultDomain;
    }

    /**
     * Resolves access roles.
     * Priority: method @NotionDoc.auth > class @NotionDoc.auth > defaultAuth.
     */
    public List<String> resolveAuthRoles(Method method, Class<?> controllerClass) {
        NotionDoc methodDoc = method.getAnnotation(NotionDoc.class);
        if (methodDoc != null && methodDoc.auth().length > 0) {
            return List.of(methodDoc.auth());
        }
        NotionDoc classDoc = controllerClass.getAnnotation(NotionDoc.class);
        if (classDoc != null && classDoc.auth().length > 0) {
            return List.of(classDoc.auth());
        }
        return defaultAuth;
    }

    /**
     * Infers domain from controller class name.
     * e.g. AuthController > AUTH, UserApiController > USER API.
     */
    static String inferDomainFromControllerName(String controllerName) {
        String name = controllerName;
        if (name.endsWith("RestController")) {
            name = name.substring(0, name.length() - "RestController".length());
        } else if (name.endsWith("Controller")) {
            name = name.substring(0, name.length() - "Controller".length());
        }
        if (name.isEmpty()) {
            return "";
        }
        return name.replaceAll("([a-z])([A-Z])", "$1 $2").toUpperCase();
    }
}
