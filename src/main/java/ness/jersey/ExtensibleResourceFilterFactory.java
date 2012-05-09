package ness.jersey;

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
