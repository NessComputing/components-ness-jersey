package com.nesscomputing.jersey.json;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import com.google.inject.Singleton;

/**
 * Jersey Provider class that allows Jackson to serialize any media type starting
 * with <code>x-ness</code>.  By default, only types that end with <code>+json</code> will
 * be matched.
 */
@Provider
@Produces("x-ness/*")
@Consumes("x-ness/*")
@Singleton
public class NessJacksonJsonProvider extends JacksonJsonProvider
{ }
