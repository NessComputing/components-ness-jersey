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
package com.nesscomputing.exception;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import com.nesscomputing.httpclient.guice.HttpClientModule;

/**
 * Add support for mapping NessApiException subclasses to and from HTTP responses.
 */
public final class NessApiExceptionModule extends AbstractModule
{
    private final Annotation httpClientAnnotation;

    public NessApiExceptionModule(final String httpClientName)
    {
        this(Names.named(httpClientName));
    }

    public NessApiExceptionModule(final Annotation httpClientAnnotation)
    {
        this.httpClientAnnotation = Preconditions.checkNotNull(httpClientAnnotation, "null binding annotation");
    }

    @Override
    protected void configure()
    {
        install(new SharedNessApiExceptionModule());

        HttpClientModule.bindNewObserver(binder(), httpClientAnnotation).to(Key.get(ExceptionObserver.class, httpClientAnnotation));
        bind(ExceptionObserver.class).annotatedWith(httpClientAnnotation).toProvider(new ExceptionObserverProvider()).in(Scopes.SINGLETON);

        // Constructing the binder creates the MapBinder, so we don't have undeclared dependencies
        // even if there end up being no bindings, just an empty map.
        NessApiExceptionBinder.of(binder(), httpClientAnnotation);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (httpClientAnnotation == null ? 0 : httpClientAnnotation.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NessApiExceptionModule other = (NessApiExceptionModule) obj;
        if (httpClientAnnotation == null) {
            if (other.httpClientAnnotation != null) {
                return false;
            }
        }
        else if (!httpClientAnnotation.equals(other.httpClientAnnotation)) {
            return false;
        }
        return true;
    }

    class ExceptionObserverProvider implements Provider<ExceptionObserver>
    {
        private Injector injector;

        @Inject
        void setInjector(final Injector injector)
        {
            this.injector = injector;
        }

        @Override
        public ExceptionObserver get()
        {
            final TypeLiteral<Map<String, Set<ExceptionReviver>>> type = new TypeLiteral<Map<String, Set<ExceptionReviver>>>() {};
            final Key<Map<String, Set<ExceptionReviver>>> mapBindingKey = Key.get(type, httpClientAnnotation);

            final ObjectMapper mapper = injector.getInstance(ObjectMapper.class);
            final Map<String, Set<ExceptionReviver>> revivers = injector.getInstance(mapBindingKey);

            return new ExceptionObserver(mapper, revivers);
        }
    }

    /**
     * Bindings that are shared by every NessApiExceptionModule.
     */
    static final class SharedNessApiExceptionModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(ResponseMapper.class);
        }

        @Override
        public int hashCode()
        {
            return SharedNessApiExceptionModule.class.hashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            return obj instanceof SharedNessApiExceptionModule;
        }
    }
}
