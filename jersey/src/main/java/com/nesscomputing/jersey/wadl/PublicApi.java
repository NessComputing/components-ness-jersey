package com.nesscomputing.jersey.wadl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare the annotated endpoint or resource as "public api", and put an attribute in the WADL.
 * Picked up by the frontdoor service, which will only allow outside consumers to see methods with
 * this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@WadlAnnotation({"authentication", "public"})
public @interface PublicApi { }
