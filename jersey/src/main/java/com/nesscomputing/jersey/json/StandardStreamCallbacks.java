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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import com.nesscomputing.callback.Callback;
import com.nesscomputing.jersey.json.JaxRsJsonStreamer.JsonEmitter;

/**
 * Standard hooks for {@link JaxRsJsonStreamer}.
 */
class StandardStreamCallbacks
{
    static final JsonEmitter<Object> DEFAULT_EMITTER = new JsonEmitter<Object>() {
        @Override
        public void emit(JsonGenerator g, Object element) throws IOException
        {
            g.writeObject(element);
        }
    };

    static Callback<JsonGenerator> RESULTS_HEADER = new Callback<JsonGenerator>() {
        @Override
        public void call(JsonGenerator jg) throws Exception
        {
            jg.writeStartObject();
            jg.writeArrayFieldStart("results");
        }
    };

    static Callback<JsonGenerator> RESULTS_FOOTER = new Callback<JsonGenerator>() {
        @Override
        public void call(JsonGenerator jg) throws Exception
        {
            jg.writeEndArray();
            jg.writeBooleanField("success", true);
            jg.writeEndObject();
        }
    };

    static Callback<JsonGenerator> ARRAY_HEADER = new Callback<JsonGenerator>() {
        @Override
        public void call(JsonGenerator jg) throws Exception
        {
            jg.writeStartArray();
        }
    };

    static Callback<JsonGenerator> ARRAY_FOOTER = new Callback<JsonGenerator>() {
        @Override
        public void call(JsonGenerator jg) throws Exception
        {
            jg.writeEndArray();
        }
    };
}
