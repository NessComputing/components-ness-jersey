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

import javax.ws.rs.ext.Provider;
import javax.xml.namespace.QName;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.research.ws.wadl.Method;

/**
 * Ness-specific extensions to the Jersey-generated WADL
 */
@Provider
public class NessWadlAnnotator extends DelegatingWadlGenerator {
    public static final String NESSAPI_XML_NS = "http://nessapi.net/xml/wadl";

    @Override
    public Method createMethod(AbstractResource r, AbstractResourceMethod m) {
        Method method = delegate.createMethod(r, m);
        RequiresAuthentication annotation = m.getMethod().getAnnotation(RequiresAuthentication.class);
        if (annotation == null) {
            annotation = r.getResourceClass().getAnnotation(RequiresAuthentication.class);
        }
        if (annotation != null) {
            method.getOtherAttributes().put(new QName(NESSAPI_XML_NS, "authentication", "ness"), annotation.value().toString());
        }
        return method;
    }
}
