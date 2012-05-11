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
package com.nesscomputing.jersey.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.Versioned;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Jersey Provider class that allows Jackson to serialize any media type starting
 * with <code>x-ness</code>.  By default, only types that end with <code>+json</code> will
 * be matched.
 */
@Provider
@Produces("x-ness/*")
@Consumes("x-ness/*")
@Singleton
public class NessJacksonJsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object>, Versioned
{
    private final JacksonJsonProvider delegate;

    @Inject
    NessJacksonJsonProvider(JacksonJsonProvider delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public Version version()
    {
        return delegate.version();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return delegate.isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException
    {
        return delegate.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    @Override
    public long getSize(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return delegate.getSize(value, type, genericType, annotations, mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return delegate.isWriteable(type, genericType, annotations, mediaType);
    }

    @Override
    public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException
    {
        delegate.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    @Override
    public String toString()
    {
        return "NessJacksonJsonProvider: " + delegate.toString();
    }
}
