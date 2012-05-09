package com.nesscomputing.jersey.wadl;

import java.util.List;

import javax.ws.rs.ext.Provider;

import com.sun.jersey.api.wadl.config.WadlGeneratorConfig;
import com.sun.jersey.api.wadl.config.WadlGeneratorDescription;

/**
 * Configure the {@link NessWadlAnnotator} so that Jersey picks it up
 */
@Provider
public class NessWadlAnnotationsConfig extends WadlGeneratorConfig {
    @Override
    public List<WadlGeneratorDescription> configure() {
        return WadlGeneratorConfig.generator(NessWadlAnnotator.class).build().configure();
    }
}
