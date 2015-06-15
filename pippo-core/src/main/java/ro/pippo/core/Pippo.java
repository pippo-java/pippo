/*
 * Copyright (C) 2014 the original author or authors.
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
package ro.pippo.core;

import ro.pippo.core.util.ServiceLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Decebal Suiu
 */
public class Pippo {

    private static final Logger log = LoggerFactory.getLogger(Pippo.class);

    private Application application;
    private WebServer server;

    public Pippo() {
        this(new Application());
    }

    public Pippo(Application application) {
        this.application = application;
        log.debug("Application '{}'", application);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                Pippo.this.stop();
            }

        });
    }

    public Application getApplication() {
        return application;
    }

    public WebServer getServer() {
        if (server == null) {
            server = ServiceLocator.locate(WebServer.class);
            if (server == null) {
                throw new PippoRuntimeException("Cannot find a WebServer");
            }

            PippoFilter pippoFilter = createPippoFilter(application);
            server.setPippoFilter(pippoFilter);

            server.init(application.getPippoSettings());
        }

        return server;
    }

    public void start() {
        if (getServer() != null) {
            log.debug("Server '{}'", server);
            server.start();
        }
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Override this method if you want to customize the PippoFilter.
     * <p/>
     * <pre>
     * protected PippoFilter createPippoFilter(Application application) {
     *     PippoFilter pippoFilter = super.createPippoFilter(application);
     *     pippoFilter.setIgnorePaths(Collections.singleton("/favicon.ico"));
     *
     *     return pippoFilter;
     * }
     * </pre>
     *
     * @param application
     * @return
     */
    protected PippoFilter createPippoFilter(Application application) {
        PippoFilter pippoFilter = new PippoFilter();
        pippoFilter.setApplication(application);

        return pippoFilter;
    }

}
