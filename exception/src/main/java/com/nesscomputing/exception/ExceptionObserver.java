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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.nesscomputing.httpclient.HttpClientObserver;
import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.logging.Log;

/**
 * Intercept exceptions that have been mapped to <code>x-ness/error</code> responses,
 * and rethrow them clientside.
 * Rudely consumes the http response body and never lets the actual response handler do anything.
 */
class ExceptionObserver extends HttpClientObserver
{
    private static final Log LOG = Log.findLog();
    private final ObjectMapper mapper;
    private final Map<String, Set<ExceptionReviver>> revivers;

    @Inject
    ExceptionObserver(ObjectMapper mapper, Map<String, Set<ExceptionReviver>> revivers) {
        this.mapper = mapper;
        this.revivers = revivers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HttpClientResponse onResponseReceived(HttpClientResponse response) throws IOException
    {
        if (StringUtils.isBlank(response.getContentType()))
        {
            return response;
        }

        final MediaType type = MediaType.valueOf(response.getContentType());
        if (    type.isCompatible(NessApiException.MEDIA_TYPE) ||
                NessApiException.LEGACY_TYPES.contains(type))
        {
            final Map<String, Object> wrapper = mapper.readValue(response.getResponseBodyAsStream(), new TypeReference<Map<String, Object>>() {});
            final Object causes = wrapper.get("causes");

            Preconditions.checkState(causes instanceof List, "bad causes");
            final List<?> causesList = (List<?>) causes;

            LOG.debug("Received error responses %s", Joiner.on('\t').join(causesList));

            Preconditions.checkState(causesList.get(0) instanceof Map, "bad cause");

            final NessApiException exn = toException((Map<String, Object>) causesList.get(0));

            if (causesList.size() > 1) {
                LOG.debug(exn, "Multi-exception found.  first exception, remainder following.");
            }

            for (int i = 1; i < causesList.size(); i++)
            {
                final NessApiException suppressed = toException((Map<String, Object>) causesList.get(1));
                LOG.debug(suppressed, "Multiple exceptions, continuation from prior backtrace...");
                exn.addSuppressed(suppressed);
            }

            throw exn;
        }

        return response;
    }

    private NessApiException toException(final Map<String, Object> fields)
    {
        final String type = ObjectUtils.toString(fields.get(NessApiException.ERROR_TYPE));

        final Set<ExceptionReviver> set = revivers.get(type);
        if (CollectionUtils.isEmpty(set))
        {
            LOG.error("Unknown exception type '%s'", type);
            return makeUnknownException(fields);
        }
        for (final ExceptionReviver er : set) {
            final NessApiException ex = er.apply(fields);
            if (ex != null)
            {
                return ex;
            }
        }
        LOG.error("No registered handler handled %s", fields);
        return makeUnknownException(fields);
    }

    private NessApiException makeUnknownException(Map<String, Object> fields)
    {
        return new UnknownNessApiException(fields);
    }
}
