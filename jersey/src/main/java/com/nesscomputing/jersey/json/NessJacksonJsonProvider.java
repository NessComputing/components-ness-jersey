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
