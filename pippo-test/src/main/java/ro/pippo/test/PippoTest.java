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
import org.junit.After;
import org.junit.Before;
import ro.pippo.core.Application;
import ro.pippo.core.Pippo;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Pippo Test marries Pippo, JUnit and RestAssured to provide a convenient
 * way to perform integration tests of your Pippo application.
 * <p>
 * PippoTest will start your Pippo application on a dynamically assigned port
 * in TEST mode for execution of your unit tests.
 * </p>
 *
 * @author Decebal Suiu
 */
public abstract class PippoTest extends RestAssured {

    private Pippo pippo;

    @Before
    public void setUp() {
        startPippo();
    }

    @After
    public void tearDown() {
        stopPippo();
    }

    public abstract Application createApplication();

    protected Pippo createPippo() {
        Application application = createApplication();
        // TODO how can I set the TEST mode?

        return new Pippo(application);
    }

    protected final Pippo getPippo() {
        if (pippo == null) {
            pippo = createPippo();

            // set server port;
            // the port numbers in the range from 0 to 1023 are the well-known ports or system ports
            int port = findAvailablePort(1024, 10000);
            pippo.getServer().getSettings().port(port);
        }

        return pippo;
    }

    protected void startPippo() {
        getPippo().start();

        // init RestAssured
        RestAssured.port = getPippo().getServer().getSettings().getPort();
    }

    protected void stopPippo() {
        // TODO fix the bug in pippo-core (a starting flag maybe)
        // it's not needed because we have an hook on shutdown
//        getPippo().stop();

        pippo = null;
    }

    protected final int findAvailablePort(int min, int max) {
        for (int port = min; port < max; port++) {
            try {
                new ServerSocket(port).close();
                return port;
            } catch (IOException e) {
                // must already be taken
            }
        }

        throw new IllegalStateException("Could not find available port in range " + min + " to " + max);
    }

}
