package io.github.lgwk42.notiondocs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies Notion documentation metadata for API endpoints.
 *
 * <p>Class-level: sets domain for all methods in the controller.</p>
 * <pre>
 * {@literal @}NotionDoc(domain = "AUTH")
 * {@literal @}RestController
 * public class AuthController { ... }
 * </pre>
 *
 * <p>Method-level: sets metadata for individual API endpoints.</p>
 * <pre>
 * {@literal @}NotionDoc(name = "Sign In", description = "Email/password login", auth = {"ALL"})
 * {@literal @}PostMapping("/sign-in")
 * public ResponseEntity login(...) { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotionDoc {

    /**
     * API display name. If empty, the method name is used.
     */
    String name() default "";

    /**
     * API description.
     */
    String description() default "";

    /**
     * Domain category (e.g. "AUTH", "MEETING", "USER").
     * When set at class-level, applies to all methods in the controller.
     * Method-level overrides class-level.
     */
    String domain() default "";

    /**
     * Access roles (e.g. {"ALL"}, {"USER", "ADMIN", "OWNER", "MASTER"}).
     */
    String[] auth() default {};

    /**
     * Response cases for this endpoint.
     * When empty, the response is inferred from the method return type.
     */
    Response[] responses() default {};
}
