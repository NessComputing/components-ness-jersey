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

import java.util.Set;

import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class ExtensibleResponseFilter implements ContainerResponseFilter {
    // Normally we do not use field injection, but Jersey does not support constructor
    // injection on container filters for some reason.
    @Inject
    Injector injector;

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        Binding<Set<ContainerResponseFilter>> filterBinding = injector.getExistingBinding(Key.get(new TypeLiteral<Set<ContainerResponseFilter>>() {}));
        if (filterBinding == null) {
            return response;
        }
        Set<ContainerResponseFilter> filters = filterBinding.getProvider().get();
        for (ContainerResponseFilter filter : filters) {
            if (filter instanceof ExtensibleResponseFilter) {
                throw new IllegalStateException("Cowardly refusing to recursively invoke ExtensibleResponseFilter.  Please fix your Guice module bindings.");
            }

            response = filter.filter(request, response);
        }
        return response;
    }
}
