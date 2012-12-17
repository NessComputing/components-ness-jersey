package com.nesscomputing.exception;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import com.google.common.collect.ImmutableList;

class NessApiExceptionEntity
{
    private final int statusCode;
    private final String statusText;
    private final List<Object> causes;

    public NessApiExceptionEntity(Status status, Object... causes)
    {
        this(status, Arrays.asList(causes));
    }

    public NessApiExceptionEntity(Status status, List<Object> causes)
    {
        this.statusCode = status.getStatusCode();
        this.statusText = status.getReasonPhrase();

        this.causes = ImmutableList.copyOf(causes);
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getStatusText()
    {
        return statusText;
    }

    public List<Object> getCauses()
    {
        return causes;
    }
}
