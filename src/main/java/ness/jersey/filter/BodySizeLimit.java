package ness.jersey.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configure on a per-resource or per-resource-method basis the maximum
 * POST / PUT body size
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface BodySizeLimit {
    /**
     * The maximum body size allowed, in bytes.  -1 means unlimited.
     */
    long value() default 0;
}
