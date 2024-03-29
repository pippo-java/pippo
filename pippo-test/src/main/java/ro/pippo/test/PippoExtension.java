/*
 * Copyright (C) 2015-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.mapper.ObjectMapper;
import io.restassured.mapper.ObjectMapperDeserializationContext;
import io.restassured.mapper.ObjectMapperSerializationContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import ro.pippo.core.Application;
import ro.pippo.core.ContentTypeEngine;
import ro.pippo.core.Pippo;
import ro.pippo.core.PippoRuntimeException;

/**
 * Start Pippo prior to test execution and stop Pippo after the tests have completed.
 *
 * @author Decebal Suiu
 */
public class PippoExtension implements BeforeAllCallback, AfterAllCallback {

    private static final Logger log = LoggerFactory.getLogger(PippoExtension.class);

    protected final Pippo pippo;

    public PippoExtension() {
        this(new Application());
    }
    /**
     * This constructor dynamically allocates a free port.
     */
    public PippoExtension(Application application) {
        this(application, AvailablePortFinder.findAvailablePort());
    }

    public PippoExtension(Application application, Integer port) {
        this(new Pippo(application), port);
    }

    public PippoExtension(Pippo pippo) {
        this(pippo, AvailablePortFinder.findAvailablePort());
    }

    public PippoExtension(Pippo pippo, Integer port) {
        this.pippo = pippo;

        pippo.getServer().setPort(port);
    }

    /**
     * Useful in case that you want to mock some services via setters.
     */
    public Application getApplication() {
        return pippo.getApplication();
    }

    public void startPippo() {
        try {
            pippo.start();
        } catch (Exception e) {
            throw new RuntimeException("Error starting Pippo", e);
        }
        initRestAssured();
    }

    public void stopPippo() {
        try {
            pippo.stop();
        } catch (Exception e) {
            log.error("Error stopping Pippo", e);
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        startPippo();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        stopPippo();
    }

    protected void initRestAssured() {
        // port
        RestAssured.port = pippo.getServer().getPort();

        // objectMapper
        ObjectMapper objectMapper = new ObjectMapper() {

            @Override
            public Object deserialize(ObjectMapperDeserializationContext context) {
                ContentTypeEngine engine = pippo.getApplication().getContentTypeEngine(context.getContentType());
                if (engine == null) {
                    throw new PippoRuntimeException("No ContentTypeEngine registered for {}", context.getContentType());
                }

                return engine.fromString(context.getDataToDeserialize().asString(), context.getType().getClass());
            }

            @Override
            public Object serialize(ObjectMapperSerializationContext context) {
                ContentTypeEngine engine = pippo.getApplication().getContentTypeEngine(context.getContentType());
                if (engine == null) {
                    throw new PippoRuntimeException("No ContentTypeEngine registered for {}", context.getContentType());
                }

                return engine.toString(context.getObjectToSerialize());
            }

        };
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig(objectMapper));
    }

}
