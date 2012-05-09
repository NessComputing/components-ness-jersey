package com.nesscomputing.jersey.wadl;

import com.google.inject.AbstractModule;

public class WadlModule extends AbstractModule {
    @Override
    protected void configure() {
        bind (FooResource.class);
    }
}
