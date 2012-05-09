package com.nesscomputing.jersey.exceptions;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.MDC;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.spi.Message;
import com.nesscomputing.logging.Log;
import com.sun.jersey.core.reflection.ReflectionHelper;

@Provider
public class GuiceProvisionExceptionMapper implements ExceptionMapper<ProvisionException>
{
    private static final Log LOG = Log.findLog();

    private final Map<Class<? extends Throwable>, ExceptionMapper<Throwable>> exceptionMappers = Maps.newHashMap();

    @Inject
    @SuppressWarnings("unchecked")
    public GuiceProvisionExceptionMapper(final Injector injector)
    {
        for (Map.Entry<Key<?>, Binding<?>> binding : injector.getAllBindings().entrySet()) {
            final Key<?> key = binding.getKey();
            final Class<?> clazz = key.getTypeLiteral().getRawType();

            Class<? extends Throwable> c = null;
            if (clazz.equals(ExceptionMapper.class)) {
                final Type type = key.getTypeLiteral().getType();
                if (type instanceof ParameterizedType) {
                    final Type [] params = ((ParameterizedType) type).getActualTypeArguments();
                    Preconditions.checkState(params != null && params.length == 1, "Not a valid Exception mapper found: %s", type);
                    c = Class.class.cast(params[0]);
                }
            }
            else {
                final Class<?> [] interfaces = clazz.getInterfaces();
                if (ArrayUtils.contains(interfaces, ExceptionMapper.class)) {
                    final Type type = key.getTypeLiteral().getType();
                    // Bind concrete classes, but skip the GuiceProvisionExceptionMapper (that would be a circular dep).
                    if (type instanceof Class && !(this.getClass().equals(type))) {
                        c = getExceptionType(Class.class.cast(type));
                    }
                }
            }

            if (c != null) {
                exceptionMappers.put(c, ExceptionMapper.class.cast(injector.getInstance(key)));
            }
        }
    }

    @Override
    public Response toResponse(final ProvisionException exception)
    {
        final Collection<Message> messages = exception.getErrorMessages();
        Throwable cause = null;
        if (CollectionUtils.isNotEmpty(messages)) {
            for (Message message : messages) {
                cause = message.getCause();
                if (cause != null) {
                    break;
                }
            }
        }
        if (cause != null) {
            LOG.trace("Mapping %s", cause.getClass().getSimpleName());
            final ExceptionMapper<Throwable> mapper = find(cause.getClass());
            if (mapper != null) {
                return mapper.toResponse(cause);
            }
        }

        // Since this is not handled by any mappers, let's complain loudly.
        LOG.error(exception, "Did not find a mapper to handle exception type %s wrapped in ProvisionException", cause != null ? cause.getClass() : "<null>");

        final Map<String, String> response = ImmutableMap.of("code", Status.INTERNAL_SERVER_ERROR.toString(),
             // XXX: this feels a tad like it violates encapsulation, but any other solution drags in tc-tracking as a dependency.
                                                             "trace", ObjectUtils.toString(MDC.get("track")),
                                                             "message", ((cause != null) ? Objects.firstNonNull(cause.getMessage(), "unknown") : "unknown"));

        return Response.status(Status.INTERNAL_SERVER_ERROR)
            .entity(response)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .build();
    }

    //
    // Straight out of ExceptionMapperFactory in the jersey code.
    //

    private ExceptionMapper<Throwable> find(Class<? extends Throwable> c)
    {
        int distance = Integer.MAX_VALUE;
        ExceptionMapper<Throwable> selectedEm = null;
        for (Map.Entry<Class<? extends Throwable>, ExceptionMapper<Throwable>> entry : exceptionMappers.entrySet()) {
            int d = distance(c, entry.getKey());
            if (d < distance) {
                distance = d;
                selectedEm = entry.getValue();
                if (distance == 0) {
                    break;
                }
            }
        }

        return selectedEm;
    }

    private int distance(Class<?> c, Class<?> emtc)
    {
        int distance = 0;
        if (!emtc.isAssignableFrom(c)) {
            return Integer.MAX_VALUE;
        }
        while (c != emtc) {
            c = c.getSuperclass();
            distance++;
        }

        return distance;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Throwable> getExceptionType(Class<?> c)
    {
        Class<?> t = getType(c);
        Preconditions.checkState(t != null, "Could not determine the type of %s", c);
        if (Throwable.class.isAssignableFrom(t)) {
            return (Class<? extends Throwable>)t;
        }

        // TODO log warning
        return null;
    }

    private Class<?> getType(Class<?> c)
    {
        Class<?> _c = c;
        while (_c != Object.class) {
            Type[] ts = _c.getGenericInterfaces();
            for (Type t : ts) {
                if (t instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)t;
                    if (pt.getRawType() == ExceptionMapper.class) {
                        return getResolvedType(pt.getActualTypeArguments()[0], c, _c);
                    }
                }
            }

            _c = _c.getSuperclass();
        }

        // This statement will never be reached
        return null;
    }

    private Class<?> getResolvedType(Type t, Class<?> c, Class<?> dc) {
        if (t instanceof Class) {
            return (Class<?>)t;
        }
        else if (t instanceof TypeVariable) {
            ReflectionHelper.ClassTypePair ct = ReflectionHelper.resolveTypeVariable(c, dc, (TypeVariable<?>)t);
            if (ct != null) {
                return ct.c;
            }
            else {
                return null;
            }
        }
        else if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)t;
            return (Class<?>)pt.getRawType();
        }
        else {
            // TODO log
            return null;
        }
    }
}
