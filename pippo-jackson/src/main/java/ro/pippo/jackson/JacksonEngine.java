/*
 * Copyright (C) 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file eTcept in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either eTpress or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import ro.pippo.core.Application;
import ro.pippo.core.ContentTypeEngine;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoRuntimeException;

import java.io.IOException;
import java.util.TimeZone;

/**
 * A JsonEngine based on Jackson.
 *
 * @author James Moger
 */
public class JacksonEngine implements ContentTypeEngine {

    protected ObjectMapper objectMapper;

    @Override
    public void init(Application application) {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setTimeZone(TimeZone.getDefault());
        objectMapper.registerModule(new AfterburnerModule());
    }

    @Override
    public String getContentType() {
        return HttpConstants.ContentType.APPLICATION_JSON;
    }

    @Override
    public String toString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new PippoRuntimeException("Error serializing object to JSON", e);
        }
    }

    @Override
    public <T> T fromString(String content, Class<T> classOfT) {
        try {
            return objectMapper.readValue(content, classOfT);
        } catch (JsonParseException | JsonMappingException ex) {
            throw new PippoRuntimeException("Error deserializing JSON", ex);
        } catch (IOException e) {
            throw new PippoRuntimeException("Invalid Json document", e);
        }
    }

}
