package io.github.lgwk42.notiondocs.scanner;

import io.github.lgwk42.notiondocs.model.ApiEndpointInfo;
import io.github.lgwk42.notiondocs.model.ControllerGroup;
import io.github.lgwk42.notiondocs.model.FieldInfo;
import io.github.lgwk42.notiondocs.model.HeaderInfo;
import io.github.lgwk42.notiondocs.model.ParameterInfo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scans all API endpoints from RequestMappingHandlerMapping and extracts metadata.
 * Delegates metadata resolution to {@link EndpointMetadataResolver}
 * and parameter extraction to {@link EndpointParameterExtractor}.
 */
public class ApiEndpointScanner {

    private final RequestMappingHandlerMapping handlerMapping;
    private final DtoFieldExtractor fieldExtractor;
    private final EndpointMetadataResolver metadataResolver;
    private final EndpointParameterExtractor parameterExtractor;
    private final List<String> includePackages;
    private final List<String> excludePackages;

    public ApiEndpointScanner(RequestMappingHandlerMapping handlerMapping,
                              DtoFieldExtractor fieldExtractor,
                              EndpointMetadataResolver metadataResolver,
                              EndpointParameterExtractor parameterExtractor,
                              List<String> includePackages,
                              List<String> excludePackages) {
        this.handlerMapping = handlerMapping;
        this.fieldExtractor = fieldExtractor;
        this.metadataResolver = metadataResolver;
        this.parameterExtractor = parameterExtractor;
        this.includePackages = includePackages != null ? includePackages : List.of();
        this.excludePackages = excludePackages != null ? excludePackages : List.of();
    }

    /**
     * Scans all controllers and returns grouped endpoint lists.
     */
    public List<ControllerGroup> scan() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        Map<Class<?>, List<Map.Entry<RequestMappingInfo, HandlerMethod>>> grouped =
                handlerMethods.entrySet().stream()
                        .filter(entry -> shouldInclude(entry.getValue().getBeanType()))
                        .collect(Collectors.groupingBy(entry -> entry.getValue().getBeanType()));
        List<ControllerGroup> groups = new ArrayList<>();
        for (Map.Entry<Class<?>, List<Map.Entry<RequestMappingInfo, HandlerMethod>>> entry : grouped.entrySet()) {
            Class<?> controllerClass = entry.getKey();
            String controllerName = controllerClass.getSimpleName();
            String basePath = extractBasePath(controllerClass);
            List<ApiEndpointInfo> endpoints = entry.getValue().stream()
                    .flatMap(e -> extractEndpoints(e.getKey(), e.getValue(), controllerClass, controllerName).stream())
                    .sorted(Comparator.comparing(ApiEndpointInfo::uri)
                            .thenComparing(ApiEndpointInfo::httpMethod))
                    .toList();
            groups.add(new ControllerGroup(controllerName, basePath, endpoints));
        }
        groups.sort(Comparator.comparing(ControllerGroup::controllerName));
        return groups;
    }

    private boolean shouldInclude(Class<?> controllerClass) {
        String packageName = controllerClass.getPackageName();
        if (packageName.startsWith("org.springframework")) {
            return false;
        }
        for (String excluded : excludePackages) {
            if (packageName.startsWith(excluded)) {
                return false;
            }
        }
        if (includePackages.isEmpty()) {
            return true;
        }
        for (String included : includePackages) {
            if (packageName.startsWith(included)) {
                return true;
            }
        }
        return false;
    }

    private String extractBasePath(Class<?> controllerClass) {
        RequestMapping mapping = controllerClass.getAnnotation(RequestMapping.class);
        if (mapping != null && mapping.value().length > 0) {
            return mapping.value()[0];
        }
        return "";
    }

    private List<ApiEndpointInfo> extractEndpoints(RequestMappingInfo mappingInfo,
                                                    HandlerMethod handlerMethod,
                                                    Class<?> controllerClass,
                                                    String controllerName) {
        Set<String> patterns = new LinkedHashSet<>();
        if (mappingInfo.getPathPatternsCondition() != null) {
            mappingInfo.getPathPatternsCondition().getPatterns()
                    .forEach(p -> patterns.add(p.getPatternString()));
        } else if (mappingInfo.getPatternsCondition() != null) {
            patterns.addAll(mappingInfo.getPatternsCondition().getPatterns());
        }
        Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
        if (methods.isEmpty()) {
            methods = Set.of(RequestMethod.GET);
        }
        Method method = handlerMethod.getMethod();
        String apiName = metadataResolver.resolveApiName(method, controllerClass);
        String description = metadataResolver.resolveDescription(method, controllerClass);
        String domain = metadataResolver.resolveDomain(method, controllerClass, controllerName);
        List<String> authRoles = metadataResolver.resolveAuthRoles(method, controllerClass);
        List<ApiEndpointInfo> endpoints = new ArrayList<>();
        for (String pattern : patterns) {
            for (RequestMethod httpMethod : methods) {
                List<HeaderInfo> headers = parameterExtractor.extractHeaders(method);
                List<ParameterInfo> pathVars = parameterExtractor.extractPathVariables(method);
                List<ParameterInfo> queryParams = parameterExtractor.extractQueryParams(method);
                Type requestBodyType = parameterExtractor.extractRequestBodyType(method);
                List<FieldInfo> requestBodyFields = requestBodyType != null
                        ? fieldExtractor.extract(requestBodyType) : List.of();
                String requestBodyTypeName = requestBodyType != null
                        ? DtoFieldExtractor.formatTypeName(requestBodyType) : null;
                Type responseType = parameterExtractor.resolveResponseType(method);
                List<FieldInfo> responseFields = responseType != null
                        ? fieldExtractor.extract(responseType) : List.of();
                String responseTypeName = responseType != null
                        ? DtoFieldExtractor.formatTypeName(responseType) : null;
                endpoints.add(new ApiEndpointInfo(
                        apiName,
                        description,
                        domain,
                        authRoles,
                        pattern,
                        httpMethod.name(),
                        headers,
                        pathVars,
                        queryParams,
                        requestBodyFields,
                        requestBodyTypeName,
                        responseFields,
                        responseTypeName,
                        controllerName,
                        method.getName()
                ));
            }
        }
        return endpoints;
    }
}
