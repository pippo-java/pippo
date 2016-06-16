/*
 * Copyright (C) 2015 the original author or authors.
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

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.mapper.ObjectMapper;
import com.jayway.restassured.mapper.ObjectMapperDeserializationContext;
import com.jayway.restassured.mapper.ObjectMapperSerializationContext;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import ro.pippo.core.Application;
import ro.pippo.core.ContentTypeEngine;
import ro.pippo.core.Pippo;
import ro.pippo.core.PippoRuntimeException;

/**
 * Start Pippo prior to test execution and stop Pippo after the tests have completed.
 *
 * @author Decebal Suiu
 */
public class PippoRule implements TestRule {

    protected final Application application;
    protected final Integer port;

    /**
     * This constructor dynamically allocates a free port.
     */
    public PippoRule(Application application) {
        this(application, AvailablePortFinder.findAvailablePort());
    }

    public PippoRule(Application application, Integer port) {
        this.application = application;
        this.port = port;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return decorateStatement(statement);
    }

    protected Pippo createPippo() {
        Pippo pippo = new Pippo(application);
        pippo.getServer().getSettings().port(port);

        return pippo;
    }

    protected void startPippo(final Pippo pippo) {
        pippo.start();

        // init RestAssured
        RestAssured.port = pippo.getServer().getSettings().getPort();
        RestAssured.objectMapper(new ObjectMapper() {

            @Override
            public Object deserialize(ObjectMapperDeserializationContext context) {
                ContentTypeEngine engine = pippo.getApplication().getContentTypeEngine(context.getContentType());
                if (engine == null) {
                    throw new PippoRuntimeException("No ContentTypeEngine registered for {}", context.getContentType());
                }

                return engine.fromString(context.getDataToDeserialize().asString(), context.getType());
            }

            @Override
            public Object serialize(ObjectMapperSerializationContext context) {
                ContentTypeEngine engine = pippo.getApplication().getContentTypeEngine(context.getContentType());
                if (engine == null) {
                    throw new PippoRuntimeException("No ContentTypeEngine registered for {}", context.getContentType());
                }

                return engine.toString(context.getObjectToSerialize());
            }

        });
    }

    protected void stopPippo(Pippo pippo) {
        // TODO fix the bug in pippo-core (a starting flag maybe)
//        pippo.stop(); // it's not needed because we have an hook on shutdown
    }

    private Statement decorateStatement(Statement statement) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                Pippo pippo = createPippo();
                startPippo(pippo);

                try {
                    statement.evaluate();
                } finally {
                    stopPippo(pippo);
                }
            }

        };
    }

}
