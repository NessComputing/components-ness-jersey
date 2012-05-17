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
package com.nesscomputing.jersey.core;

import java.util.Map;

import javax.servlet.ServletException;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;

/**
 * A {@link GuiceContainer} that pulls its ResourceConfig out of Guice.
 */
@Singleton
public class NessGuiceContainer extends GuiceContainer
{
    private final ResourceConfig resourceConfig;

    @Inject
    public NessGuiceContainer(final ResourceConfig resourceConfig,
                              final Injector injector)
    {
        super(injector);

        this.resourceConfig = resourceConfig;
    }

    @Override
    protected ResourceConfig getDefaultResourceConfig(final Map<String, Object> props,
                                                      final WebConfig webConfig)
        throws ServletException
    {
        return resourceConfig;
    }
}
