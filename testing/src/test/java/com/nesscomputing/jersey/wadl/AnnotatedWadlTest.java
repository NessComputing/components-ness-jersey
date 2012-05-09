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
package com.nesscomputing.jersey.wadl;

import static org.junit.Assert.assertEquals;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.response.StringContentConverter;
import com.nesscomputing.httpclient.testing.CapturingHttpResponseHandler;
import com.nesscomputing.httpserver.HttpServer;
import com.nesscomputing.testing.IntegrationTestRule;
import com.nesscomputing.testing.IntegrationTestRuleBuilder;
import com.nesscomputing.testing.ServiceDefinition;
import com.nesscomputing.testing.ServiceDefinitionBuilder;
import com.nesscomputing.testing.lessio.AllowDNSResolution;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;

@AllowNetworkAccess(endpoints= {"127.0.0.1:*"})
@AllowDNSResolution
public class AnnotatedWadlTest {

    private final ServiceDefinition wadlDefn = new ServiceDefinitionBuilder().addModule(new WadlModule()).build();

    @Rule
    public IntegrationTestRule test = new IntegrationTestRuleBuilder()
        .addService("wadl", wadlDefn)
        .build(this);

    @Inject
    HttpClient httpClient;

    CapturingHttpResponseHandler handler = new CapturingHttpResponseHandler();

    private String baseUrl;

    @Before
    public void figureOutUrl() {
        HttpServer server = test.exposeBinding("wadl", Key.get(HttpServer.class));
        baseUrl = "http://localhost:" + server.getInternalHttpPort();
    }

    @Test
    public void testWadlAnnotated() throws Exception {
        String xml = httpClient.get(baseUrl + "/application.wadl", StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform();
        Builder parser = new Builder();
        Document doc = parser.build(xml, null);

        Element applicationElement = doc.getRootElement();
        Element resourcesElement = applicationElement.getFirstChildElement("resources", "http://wadl.dev.java.net/2009/02");
        Elements resources = resourcesElement.getChildElements("resource", "http://wadl.dev.java.net/2009/02");
        for (int i = 0; i < resources.size(); i++) {
            Elements methods = resources.get(i).getChildElements("method", "http://wadl.dev.java.net/2009/02");
            for (int j = 0; j < methods.size(); j++) {
                Element method = methods.get(j);
                String id = method.getAttribute("id").getValue();
                Attribute auth = method.getAttribute("authentication", "http://nessapi.net/xml/wadl");

                if (auth == null) {
                    continue;
                }
                else  if (id.equals("echoGet")) {
                    assertEquals("NONE", auth.getValue());
                }
                else if (id.equals("echo")) {
                    assertEquals("REQUIRED", auth.getValue());
                }
                else {
                    throw new AssertionError(method);
                }
            }
        }
    }
}
