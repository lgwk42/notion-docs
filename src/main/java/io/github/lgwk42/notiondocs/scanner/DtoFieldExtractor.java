package io.github.lgwk42.notiondocs.scanner;

import io.github.lgwk42.notiondocs.model.FieldInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Extracts field metadata from DTO classes using reflection.
 * Supports Java records and POJOs, recursively resolving nested objects.
 */
public class DtoFieldExtractor {

    private static final Set<Class<?>> TERMINAL_TYPES = Set.of(
            String.class, Boolean.class, boolean.class,
            Byte.class, byte.class, Short.class, short.class,
            Integer.class, int.class, Long.class, long.class,
            Float.class, float.class, Double.class, double.class,
            Character.class, char.class,
            BigDecimal.class, BigInteger.class,
            UUID.class, Date.class,
            LocalDate.class, LocalDateTime.class, LocalTime.class,
            ZonedDateTime.class, OffsetDateTime.class, Instant.class
    );

    /**
     * Extracts fields from the given type.
     */
    public List<FieldInfo> extract(Type type) {
        return extract(type, new HashSet<>());
    }

    private List<FieldInfo> extract(Type type, Set<Class<?>> visited) {
        Class<?> rawClass = resolveRawClass(type);
        if (rawClass == null || isTerminal(rawClass) || rawClass == void.class || rawClass == Void.class) {
            return List.of();
        }
        if (Collection.class.isAssignableFrom(rawClass) || rawClass.isArray()) {
            Type elementType = resolveCollectionElementType(type, rawClass);
            if (elementType != null) {
                return extract(elementType, visited);
            }
            return List.of();
        }
        if (Map.class.isAssignableFrom(rawClass)) {
            return List.of();
        }
        if (!visited.add(rawClass)) {
            return List.of();
        }
        try {
            List<FieldInfo> fields = new ArrayList<>();
            if (rawClass.isRecord()) {
                extractFromRecord(rawClass, fields, visited);
            } else {
                extractFromPojo(rawClass, fields, visited);
            }
            return Collections.unmodifiableList(fields);
        } finally {
            visited.remove(rawClass);
        }
    }

    private void extractFromRecord(Class<?> recordClass, List<FieldInfo> fields, Set<Class<?>> visited) {
        for (RecordComponent component : recordClass.getRecordComponents()) {
            String name = component.getName();
            Type genericType = component.getGenericType();
            String typeName = formatTypeName(genericType);
            boolean required = isRequired(component.getAnnotations());
            if (!required) {
                try {
                    Field field = recordClass.getDeclaredField(name);
                    required = isRequired(field.getAnnotations());
                } catch (NoSuchFieldException ignored) {
                }
            }
            List<FieldInfo> children = resolveChildren(genericType, visited);
            fields.add(new FieldInfo(name, typeName, required, children));
        }
    }

    private void extractFromPojo(Class<?> clazz, List<FieldInfo> fields, Set<Class<?>> visited) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                String name = resolveFieldName(field);
                Type genericType = field.getGenericType();
                String typeName = formatTypeName(genericType);
                boolean required = isRequired(field.getAnnotations());
                List<FieldInfo> children = resolveChildren(genericType, visited);
                fields.add(new FieldInfo(name, typeName, required, children));
            }
            current = current.getSuperclass();
        }
    }

    private List<FieldInfo> resolveChildren(Type type, Set<Class<?>> visited) {
        Class<?> raw = resolveRawClass(type);
        if (raw == null || isTerminal(raw) || raw.isEnum()) {
            return List.of();
        }
        if (Collection.class.isAssignableFrom(raw) || raw.isArray()) {
            Type elementType = resolveCollectionElementType(type, raw);
            if (elementType != null) {
                return extract(elementType, visited);
            }
            return List.of();
        }
        return extract(type, visited);
    }

    private boolean isRequired(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            String name = annotation.annotationType().getSimpleName();
            if ("NotNull".equals(name) || "NotBlank".equals(name) || "NotEmpty".equals(name)) {
                return true;
            }
        }
        return false;
    }

    private String resolveFieldName(Field field) {
        for (Annotation annotation : field.getAnnotations()) {
            if ("JsonProperty".equals(annotation.annotationType().getSimpleName())) {
                try {
                    Method valueMethod = annotation.annotationType().getMethod("value");
                    String value = (String) valueMethod.invoke(annotation);
                    if (value != null && !value.isEmpty()) {
                        return value;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return field.getName();
    }

    private boolean isTerminal(Class<?> clazz) {
        return TERMINAL_TYPES.contains(clazz) || clazz.isPrimitive() || clazz.isEnum();
    }

    static Class<?> resolveRawClass(Type type) {
        if (type instanceof Class<?> c) {
            return c;
        }
        if (type instanceof ParameterizedType pt) {
            Type raw = pt.getRawType();
            if (raw instanceof Class<?> c) {
                return c;
            }
        }
        return null;
    }

    private Type resolveCollectionElementType(Type type, Class<?> rawClass) {
        if (rawClass.isArray()) {
            return rawClass.getComponentType();
        }
        if (type instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (args.length > 0) {
                return args[0];
            }
        }
        return null;
    }

    /**
     * Formats a Type into a human-readable string.
     * e.g. "List&lt;UserResponse&gt;", "String", "Map&lt;String, Object&gt;"
     */
    static String formatTypeName(Type type) {
        if (type instanceof Class<?> c) {
            return c.getSimpleName();
        }
        if (type instanceof ParameterizedType pt) {
            Class<?> raw = resolveRawClass(pt);
            String rawName = raw != null ? raw.getSimpleName() : pt.getRawType().getTypeName();
            Type[] args = pt.getActualTypeArguments();
            if (args.length == 0) {
                return rawName;
            }
            StringJoiner joiner = new StringJoiner(", ");
            for (Type arg : args) {
                joiner.add(formatTypeName(arg));
            }
            return rawName + "<" + joiner + ">";
        }
        if (type instanceof WildcardType wt) {
            Type[] upper = wt.getUpperBounds();
            if (upper.length > 0 && upper[0] != Object.class) {
                return "? extends " + formatTypeName(upper[0]);
            }
            return "?";
        }
        return type.getTypeName();
    }
}
