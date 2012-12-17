package com.nesscomputing.exception;

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;

/**
 * Register NessApiException subclasses so they may be correctly mapped to and from HTTP responses.
 */
public class NessApiExceptionBinder
{
    private final Binder binder;

    public NessApiExceptionBinder(Binder binder) {
        this.binder = binder;
    }

    public static void registerExceptionClass(Binder binder, Class<? extends NessApiException> klass)
    {
        new NessApiExceptionBinder(binder).registerExceptionClass(klass);
    }

    public void registerExceptionClass(Class<? extends NessApiException> klass)
    {
        final ExceptionReviver predicate = new ExceptionReviver(klass);
        MapBinder.newMapBinder(binder, String.class, ExceptionReviver.class).permitDuplicates()
            .addBinding(predicate.getMatchedType()).toInstance(predicate);
    }
}
