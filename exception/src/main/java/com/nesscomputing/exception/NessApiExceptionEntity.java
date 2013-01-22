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

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response.StatusType;

import com.google.common.collect.ImmutableList;

class NessApiExceptionEntity
{
    private final int statusCode;
    private final String statusText;
    private final List<Object> causes;

    public NessApiExceptionEntity(StatusType status, Object... causes)
    {
        this(status, Arrays.asList(causes));
    }

    public NessApiExceptionEntity(StatusType status, List<Object> causes)
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
