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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.route.ResourceRouting;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteGroup;
import ro.pippo.core.util.ServiceLocator;

/**
 * @author Decebal Suiu
 */
public class Pippo implements ResourceRouting {

    private static final Logger log = LoggerFactory.getLogger(Pippo.class);

    private Application application;
    private WebServer server;
    private volatile boolean running;

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
            WebServer server = ServiceLocator.locate(WebServer.class);
            if (server == null) {
                throw new PippoRuntimeException("Cannot find a WebServer");
            }

            setServer(server);
        }

        return server;
    }

    /**
     * Entry point for a custom WebServer.
     * The idea is to create a custom WebServer if you want to override some aspects (method) of that server or
     * if you want free access to the servlet container (Jetty, Tomcat, ...).
     *
     * <p>
     * Show below the code for a <@code>JettyServer</@code> with persistent sessions.
     * </p>
     *
     * <pre>
     * <@code>
     * public class MyJettyServer extends JettyServer {
     *
     *     @Override
     *     protected ServletContextHandler createPippoHandler() {
     *         ServletContextHandler handler = super.createPippoHandler();
     *
     *         // set session manager with persistence
     *         HashSessionManager sessionManager = new HashSessionManager();
     *         try {
     *             sessionManager.setStoreDirectory(new File("sessions-storage"));
     *         } catch (IOException e) {
     *             throw new PippoRuntimeException(e);
     *         }
     *         sessionManager.setLazyLoad(true); // other possible option
     *         handler.setSessionHandler(new SessionHandler(sessionManager));
     *
     *         return handler;
     *     }
     *
     * }
     *
     * public class Main {
     *
     *     public static void main(String[] args) {
     *         new Pippo(new MyApplication()).setServer(new MyJettyServer()).start();
     *     }
     *
     * }
     * </@code>
     * </pre>
     *
     * @param server
     * @return
     */
    public Pippo setServer(WebServer server) {
        this.server = server;

        PippoFilter pippoFilter = createPippoFilter(application);
        PippoSettings pippoSettings = application.getPippoSettings();
        this.server.setPippoFilter(pippoFilter).init(pippoSettings);

        return this;
    }

    public void start() {
        if (running) {
            log.warn("Server is already started ");
            return;
        }

        WebServer server = getServer();
        log.debug("Start server '{}'", server);
        server.start();
        running = true;
    }

    public void stop() {
        if (!running) {
            log.warn("Server is not started");
            return;
        }

        WebServer server = getServer();
        log.debug("Stop server '{}'", server);
        server.stop();
        running = false;
    }

    @Override
    public void addRoute(Route route) {
        getApplication().addRoute(route);
    }

    @Override
    public void addRouteGroup(RouteGroup routeGroup) {
        getApplication().addRouteGroup(routeGroup);
    }

    /**
     * Create a pippo instance, add a route on "/" that responds with a message.
     *
     * @param text
     * @return
     */
    public static Pippo send(final String text) {
        Pippo pippo = new Pippo();
        pippo.GET("/", (routeContext) -> routeContext.send(text));
        pippo.start();

        return pippo;
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
