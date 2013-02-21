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

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

/**
 * Register NessApiException subclasses so they may be correctly mapped to and from HTTP responses.
 */
public class NessApiExceptionBinder
{
    private final MapBinder<String, ExceptionReviver> mapBinder;

    /**
     * @deprecated This uses the default HttpClientObserver binding which affects all HttpClients,
     * a very dangerous thing.
     */
    @Deprecated
    public NessApiExceptionBinder(Binder binder)
    {
        this(binder, null);
    }

    private NessApiExceptionBinder(Binder binder, Annotation httpClientAnnotation)
    {
        if (httpClientAnnotation == null) {
            mapBinder = MapBinder.newMapBinder(binder, String.class, ExceptionReviver.class).permitDuplicates();
        } else {
            mapBinder = MapBinder.newMapBinder(binder, String.class, ExceptionReviver.class, httpClientAnnotation).permitDuplicates();
        }
    }

    public static NessApiExceptionBinder of(Binder binder, String httpClientName)
    {
        return of(binder, Names.named(httpClientName));
    }

    public static NessApiExceptionBinder of(Binder binder, Annotation httpClientAnnotation)
    {
        return new NessApiExceptionBinder(binder, httpClientAnnotation);
    }

    /**
     * @deprecated This uses the default HttpClientObserver binding which affects all HttpClients,
     * a very dangerous thing.
     */
    @Deprecated
    public static void registerExceptionClass(Binder binder, Class<? extends NessApiException> klass)
    {
        new NessApiExceptionBinder(binder).registerExceptionClass(klass);
    }

    public static void registerExceptionClass(Binder binder, Annotation httpClientAnnotation, Class<? extends NessApiException> klass)
    {
        of(binder, httpClientAnnotation).registerExceptionClass(klass);
    }

    public static void registerExceptionClass(Binder binder, String httpClientName, Class<? extends NessApiException> klass)
    {
        of(binder, httpClientName).registerExceptionClass(klass);
    }

    public void registerExceptionClass(Class<? extends NessApiException> klass)
    {
        final ExceptionReviver predicate = new ExceptionReviver(klass);
        mapBinder.addBinding(predicate.getMatchedType()).toInstance(predicate);
    }
}
