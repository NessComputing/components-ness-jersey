package ness.jersey;

import java.util.Set;

import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class ExtensibleRequestFilter implements ContainerRequestFilter {
    // Normally we do not use field injection, but Jersey does not support constructor
    // injection on container filters for some reason.
    @Inject
    Injector injector;

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        Binding<Set<ContainerRequestFilter>> filterBinding = injector.getExistingBinding(Key.get(new TypeLiteral<Set<ContainerRequestFilter>>() {}));
        if (filterBinding == null) {
            return request;
        }
        Set<ContainerRequestFilter> filters = filterBinding.getProvider().get();
        for (ContainerRequestFilter filter : filters) {
            if (filter instanceof ExtensibleRequestFilter) {
                throw new IllegalStateException("Cowardly refusing to recursively invoke ExtensibleRequestFilter.  Please fix your Guice module bindings.");
            }
            request = filter.filter(request);
        }
        return request;
    }
}
