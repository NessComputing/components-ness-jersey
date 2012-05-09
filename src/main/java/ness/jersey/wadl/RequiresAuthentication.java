package ness.jersey.wadl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;

/**
 * Declare the annotated endpoint or resource as "public api", and put an attribute in the WADL.
 * Picked up by the frontdoor service, which will only allow outside consumers to see methods with
 * this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequiresAuthentication {
    public enum AuthenticationType {
        /**
         * The method requires valid authentication and authorization
         */
        REQUIRED,
        /**
         * The method does not require any sort of authentication or authorization
         */
        NONE
    }

    AuthenticationType value() default AuthenticationType.REQUIRED;
}
