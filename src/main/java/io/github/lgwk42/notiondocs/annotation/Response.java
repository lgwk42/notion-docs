package io.github.lgwk42.notiondocs.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a single response case for an API endpoint.
 * Used inside {@link NotionDoc#responses()}.
 *
 * <pre>
 * {@literal @}NotionDoc(
 *     name = "Get Member",
 *     responses = {
 *         {@literal @}Response(status = 200, description = "Success", body = MemberResponse.class),
 *         {@literal @}Response(status = 404, description = "Not found", body = ErrorResponse.class)
 *     }
 * )
 * </pre>
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Response {

    /**
     * HTTP status code (e.g. 200, 404, 500).
     */
    int status() default 200;

    /**
     * Description of this response case (e.g. "Success", "Not found").
     */
    String description() default "";

    /**
     * Response body class. Use {@code void.class} for no body.
     */
    Class<?> body() default void.class;
}
