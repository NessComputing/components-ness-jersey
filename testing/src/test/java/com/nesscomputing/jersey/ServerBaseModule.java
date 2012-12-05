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

import com.google.inject.AbstractModule;
import com.sun.jersey.guice.JerseyServletModule;

import com.nesscomputing.config.Config;
import com.nesscomputing.jersey.exceptions.NessJerseyExceptionMapperModule;
import com.nesscomputing.jersey.filter.BodySizeLimitResourceFilterFactory;
import com.nesscomputing.jersey.json.NessJacksonJsonProvider;

public class ServerBaseModule extends AbstractModule
{
    private final Config config;

    public ServerBaseModule(Config config)
    {
        this.config = config;
    }

    @Override
    protected void configure()
    {
        install(new JerseyServletModule());
        install(new NessJerseyServletModule(config, "/*"));
        install (new NessJerseyExceptionMapperModule());

        NessJerseyBinder.bindResourceFilterFactory(binder()).to(BodySizeLimitResourceFilterFactory.class);

        bind (NessJacksonJsonProvider.class);
    }
}
