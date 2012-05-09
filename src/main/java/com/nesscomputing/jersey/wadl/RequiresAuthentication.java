/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.jersey.wadl;

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
