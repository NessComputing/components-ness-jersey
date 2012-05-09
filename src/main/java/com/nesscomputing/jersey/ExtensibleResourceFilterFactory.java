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

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * A single {@link ResourceFilterFactory} which inspects Guice bindings for additional ones
 * (in a Multibinder) and then delegates to them.
 */
public class ExtensibleResourceFilterFactory implements ResourceFilterFactory {

    @Inject
    Injector injector;

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        List<ResourceFilter> result = Lists.newArrayList();
        Binding<Set<ResourceFilterFactory>> binding = injector.getExistingBinding(Key.get(new TypeLiteral<Set<ResourceFilterFactory>>() {}));

        if (binding == null) {
            return result;
        }
        for (ResourceFilterFactory factory : binding.getProvider().get()) {
            if (factory instanceof ExtensibleResourceFilterFactory) {
                throw new IllegalStateException("Cowardly refusing to recursively invoke ExtensibleResourceFilterFactory.  Please fix your Guice module bindings.");
            }

            result.addAll(factory.create(am));
        }

        return result;
    }
}
