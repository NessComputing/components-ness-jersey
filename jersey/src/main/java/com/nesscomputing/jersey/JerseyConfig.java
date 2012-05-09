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
package com.nesscomputing.jersey;

import org.skife.config.Config;
import org.skife.config.Default;

/**
 * Enable and/or disable various features of the Jersey servlet.
 */
public abstract class JerseyConfig
{
    @Config("ness.jersey.trace.enabled")
    @Default("false")
    public boolean isTraceEnabled()
    {
        return false;
    }

    @Config("ness.jersey.trace-per-request.enabled")
    @Default("false")
    public boolean isTracePerRequestEnabled()
    {
        return false;
    }

    @Config("ness.jersey.generate-wadl.enabled")
    @Default("true")
    public boolean isGenerateWadlEnabled()
    {
        return true;
    }

    /**
     * Enables gzip. This is usually false, so that the container
     * can handle that.
     */
    @Config("ness.jersey.gzip.enabled")
    @Default("false")
    public boolean isGzipEnabled()
    {
        return false;
    }

    @Config("ness.jersey.logging.enabled")
    @Default("false")
    public boolean isLoggingEnabled()
    {
        return false;
    }
}
