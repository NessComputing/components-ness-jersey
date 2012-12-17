package com.nesscomputing.exception;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.ObjectUtils;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * API exception base class which has automatic transparency through
 * Jersey and HttpClient.  Attempts to remain mostly human-consumable.
 */
public abstract class NessApiException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public static final String ERROR_TYPE = "errorType";
    public static final String ERROR_SUBTYPE = "errorSubtype";
    public static final String FIELD = "field";
    public static final String DETAIL = "detail";

    public static final MediaType MEDIA_TYPE = MediaType.valueOf("x-ness/error+json");
    static final Set<MediaType> LEGACY_TYPES = ImmutableSet.of(MediaType.valueOf("x-ness/user-exception+json"));

    private final Map<String, Object> fields;

    protected NessApiException(Map<String, ? extends Object> fields)
    {
        checkNotBlank(fields, ERROR_TYPE);
        checkNotBlank(fields, ERROR_SUBTYPE);
        this.fields = ImmutableMap.copyOf(fields);
    }

    public abstract Status getStatus();

    public String getErrorType()
    {
        return getValue(ERROR_TYPE);
    }

    public String getErrorSubtype()
    {
        return getValue(ERROR_SUBTYPE);
    }

    public String getField()
    {
        return getValue(FIELD);
    }

    public String getDetail()
    {
        return getValue(DETAIL);
    }

    private String getValue(String field)
    {
        return ObjectUtils.toString(fields.get(field), null);
    }

    @JsonValue
    public Map<String, Object> getJsonRepresentation()
    {
        return fields;
    }

    private void checkNotBlank(Map<String, ? extends Object> fields, String fieldName)
    {
        Preconditions.checkArgument(!isBlank(ObjectUtils.toString(fields.get(fieldName))), "Field %s may not be missing", fieldName);
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + " [fields=" + fields + "]";
    }
}
