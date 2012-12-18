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

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;

/**
 * Take a http response and attempt to turn it back into a NessApiException subclass.
 */
class ExceptionReviver implements Function<Map<String, Object>, NessApiException>
{
    private final String type;
    private final Set<String> subtype;
    private final Constructor<? extends NessApiException> constructor;

    ExceptionReviver(Class<? extends NessApiException> klass)
    {
        final ExceptionType typeAnnotation = checkNotNull(klass.getAnnotation(ExceptionType.class), "NessApiExceptions must be annotated with @ExceptionType and optionally @ExceptionSubtype");
        final ExceptionSubtype subtypeAnnotation = klass.getAnnotation(ExceptionSubtype.class);

        type = checkNotNull(typeAnnotation.value());
        subtype = subtypeAnnotation != null ? ImmutableSet.copyOf(subtypeAnnotation.value()) : null;

        try {
            constructor = klass.getDeclaredConstructor(Map.class);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("NessApiExceptions must declare a single-argument constructor taking Map<String, Object> to turn a HTTP response back into an exception", e);
        }
    }

    @Nonnull
    public String getMatchedType()
    {
        return type;
    }

    @Override
    @Nullable
    public NessApiException apply(@Nullable Map<String, Object> input)
    {
        if (input != null && type.equals(input.get(NessApiException.ERROR_TYPE)) &&
                (subtype == null || subtype.contains(input.get(NessApiException.ERROR_SUBTYPE))))
        {
            try {
                return constructor.newInstance(input);
            } catch (final InvocationTargetException e) {
                Throwables.propagateIfPossible(e.getCause());
                Throwables.propagate(e);
            } catch (InstantiationException | IllegalAccessException e) {
                Throwables.propagate(e);
            }
        }

        return null;
    }
}
