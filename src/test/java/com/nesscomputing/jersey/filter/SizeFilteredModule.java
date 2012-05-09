package com.nesscomputing.jersey.filter;

import com.google.inject.AbstractModule;

public class SizeFilteredModule extends AbstractModule {
    @Override
    protected void configure() {

        bind (SizeResource.class);
    }
}
