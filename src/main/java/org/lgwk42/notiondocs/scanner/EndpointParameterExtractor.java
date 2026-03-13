package org.lgwk42.notiondocs.scanner;

import org.lgwk42.notiondocs.model.HeaderInfo;
import org.lgwk42.notiondocs.model.ParameterInfo;
import org.lgwk42.notiondocs.model.ParameterInfo.ParamType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts parameter metadata (headers, path variables, query params, request body, response type)
 * from handler methods.
 */
public class EndpointParameterExtractor {

    public List<HeaderInfo> extractHeaders(Method method) {
        List<HeaderInfo> headers = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            RequestHeader annotation = param.getAnnotation(RequestHeader.class);
            if (annotation != null) {
                String name = annotation.value().isEmpty() ? annotation.name() : annotation.value();
                if (name.isEmpty()) {
                    name = param.getName();
                }
                String defaultValue = ValueConstants.DEFAULT_NONE.equals(annotation.defaultValue())
                        ? null : annotation.defaultValue();
                headers.add(new HeaderInfo(
                        name,
                        param.getType().getSimpleName(),
                        annotation.required() && defaultValue == null,
                        defaultValue
                ));
            }
        }
        return headers;
    }

    public List<ParameterInfo> extractPathVariables(Method method) {
        List<ParameterInfo> params = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            PathVariable annotation = param.getAnnotation(PathVariable.class);
            if (annotation != null) {
                String name = annotation.value().isEmpty() ? annotation.name() : annotation.value();
                if (name.isEmpty()) {
                    name = param.getName();
                }
                params.add(new ParameterInfo(
                        name,
                        param.getType().getSimpleName(),
                        annotation.required(),
                        null,
                        ParamType.PATH_VARIABLE
                ));
            }
        }
        return params;
    }

    public List<ParameterInfo> extractQueryParams(Method method) {
        List<ParameterInfo> params = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            RequestParam annotation = param.getAnnotation(RequestParam.class);
            if (annotation != null) {
                String name = annotation.value().isEmpty() ? annotation.name() : annotation.value();
                if (name.isEmpty()) {
                    name = param.getName();
                }
                String defaultValue = ValueConstants.DEFAULT_NONE.equals(annotation.defaultValue())
                        ? null : annotation.defaultValue();
                params.add(new ParameterInfo(
                        name,
                        param.getType().getSimpleName(),
                        annotation.required() && defaultValue == null,
                        defaultValue,
                        ParamType.QUERY_PARAMETER
                ));
            }
        }
        return params;
    }

    /**
     * Extracts the type of the parameter annotated with {@link RequestBody}.
     */
    public Type extractRequestBodyType(Method method) {
        for (Parameter param : method.getParameters()) {
            if (param.isAnnotationPresent(RequestBody.class)) {
                return param.getParameterizedType();
            }
        }
        return null;
    }

    /**
     * Resolves the response type. Unwraps ResponseEntity&lt;T&gt; to T, returns null for void.
     */
    public Type resolveResponseType(Method method) {
        Type returnType = method.getGenericReturnType();
        Class<?> rawReturn = method.getReturnType();
        if (rawReturn == void.class || rawReturn == Void.class) {
            return null;
        }
        if (ResponseEntity.class.isAssignableFrom(rawReturn) && returnType instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (args.length > 0) {
                Type inner = args[0];
                Class<?> innerRaw = DtoFieldExtractor.resolveRawClass(inner);
                if (innerRaw == Void.class || innerRaw == void.class) {
                    return null;
                }
                return inner;
            }
            return null;
        }
        return returnType;
    }
}
