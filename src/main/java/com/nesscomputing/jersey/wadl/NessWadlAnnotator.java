package com.nesscomputing.jersey.wadl;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.xml.namespace.QName;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.server.wadl.ApplicationDescription;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.Representation;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;

/**
 * Ness-specific extensions to the Jersey-generated WADL
 */
@Provider
public class NessWadlAnnotator implements WadlGenerator {
    public static final String NESSAPI_XML_NS = "http://nessapi.net/xml/wadl";
    private WadlGenerator delegate;

    @Override
    public void setWadlGeneratorDelegate(WadlGenerator delegate) {
        this.delegate = delegate;
    }

    @Override
    public void init() throws Exception {
        delegate.init();
    }

    @Override
    public String getRequiredJaxbContextPath() {
        return delegate.getRequiredJaxbContextPath();
    }

    @Override
    public Application createApplication() {
        return delegate.createApplication();
    }

    @Override
    public Resources createResources() {
        return delegate.createResources();
    }

    @Override
    public Resource createResource(AbstractResource r, String path) {
        return delegate.createResource(r, path);
    }

    @Override
    public Method createMethod(AbstractResource r, AbstractResourceMethod m) {
        Method method = delegate.createMethod(r, m);
        RequiresAuthentication annotation = m.getMethod().getAnnotation(RequiresAuthentication.class);
        if (annotation == null) {
            annotation = r.getResourceClass().getAnnotation(RequiresAuthentication.class);
        }
        if (annotation != null) {
            method.getOtherAttributes().put(new QName(NESSAPI_XML_NS, "authentication", "ness"), annotation.value().toString());
        }
        return method;
    }

    @Override
    public Request createRequest(AbstractResource r, AbstractResourceMethod m) {
        return delegate.createRequest(r, m);
    }

    @Override
    public Representation createRequestRepresentation(AbstractResource r, AbstractResourceMethod m, MediaType mediaType) {
        return delegate.createRequestRepresentation(r, m, mediaType);
    }

    @Override
    public List<Response> createResponses(AbstractResource r, AbstractResourceMethod m) {
        return delegate.createResponses(r, m);
    }

    @Override
    public Param createParam(AbstractResource r, AbstractMethod m, Parameter p) {
        return delegate.createParam(r, m, p);
    }

    @Override
    public ExternalGrammarDefinition createExternalGrammar() {
        return delegate.createExternalGrammar();
    }

    @Override
    public void attachTypes(ApplicationDescription description) {
        delegate.attachTypes(description);
    }

}