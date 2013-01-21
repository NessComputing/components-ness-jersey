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

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.ext.Provider;
import javax.xml.namespace.QName;

import com.google.common.base.Throwables;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.research.ws.wadl.Method;

import org.apache.commons.lang3.ArrayUtils;

import com.nesscomputing.logging.Log;

/**
 * Ness-specific extensions to the Jersey-generated WADL
 */
@Provider
public class NessWadlAnnotator extends DelegatingWadlGenerator {
    private static final Log LOG = Log.findLog();

    public static final String NESSAPI_XML_NS = "http://nessapi.net/xml/wadl";
    public static final String NESSAPI_XML_PREFIX = "ness";

    @Override
    public Method createMethod(AbstractResource r, AbstractResourceMethod m) {
        Method method = delegate.createMethod(r, m);

        Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

        for (Annotation a : r.getResourceClass().getAnnotations()) {
            Class<? extends Annotation> type = a.annotationType();
            if (type.getAnnotation(WadlAnnotation.class) != null) {
                annotations.put(type, a);
            }
        }
        for (Annotation a : m.getMethod().getAnnotations()) {
            Class<? extends Annotation> type = a.annotationType();
            if (type.getAnnotation(WadlAnnotation.class) != null) {
                annotations.put(type, a);
            }
        }

        for (Annotation a : annotations.values()) {
            WadlAnnotation wadlAnnotation = checkNotNull(a.annotationType().getAnnotation(WadlAnnotation.class));
            Map<QName, String> attr = method.getOtherAttributes();
            String value = annotationStringValue(a);

            for (String xmlElement : wadlAnnotation.value()) {
                attr.put(new QName(NESSAPI_XML_NS, xmlElement, NESSAPI_XML_PREFIX), value);
            }
        }

        return method;
    }

    /**
     * Return the annotation's <code>value()</code> as a String if present, else
     * the literal "true".
     */
    private String annotationStringValue(Annotation a)
    {
        java.lang.reflect.Method valueMethod = null;
        try {
            valueMethod = a.annotationType().getMethod("value", new Class[0]);
        } catch (NoSuchMethodException e) {
            LOG.trace("No value method on %s, just using \"true\"", a.annotationType());
        }

        if (valueMethod != null)
        {
            Object result;
            try {
                result = valueMethod.invoke(a);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw Throwables.propagate(e);
            }

            if (result.getClass().isArray()) {
                return ArrayUtils.toString(result);
            }
            return Objects.toString(result);
        }

        return "true";
    }
}
