package com.nesscomputing.exception;

import java.util.Map;

import javax.ws.rs.core.Response.Status;

/**
 * We found a NessApiException, but there was no registered Reviver for it.
 */
final class UnknownNessApiException extends NessApiException
{
    protected UnknownNessApiException(Map<String, Object> fields)
    {
        super(fields);
    }

    private static final long serialVersionUID = 1L;

    @Override
    public Status getStatus()
    {
        return Status.INTERNAL_SERVER_ERROR;
    }
}
