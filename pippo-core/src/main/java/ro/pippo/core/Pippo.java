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
import ro.pippo.core.reload.ReloadClassLoader;
import ro.pippo.core.reload.ReloadWatcher;
import ro.pippo.core.route.ResourceRouting;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteGroup;
import ro.pippo.core.util.ServiceLocator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Decebal Suiu
 */
public class Pippo implements ResourceRouting, ReloadWatcher.Listener {

    private static final Logger log = LoggerFactory.getLogger(Pippo.class);

    private Application application;
    private WebServer server;
    private volatile boolean running;
    private volatile boolean reloading;

    private ReloadWatcher reloadWatcher;

    public Pippo() {
        addShutdownHook();
    }

    public Pippo(Application application) {
        this.application = application;
        log.debug("Application '{}'", application);

        addShutdownHook();
    }

    public Application getApplication() {
        if (application == null) {
            initReloading();
            if (reloading) {
                log.info("Reload enabled");
                application = createApplication();
            } else {
                log.debug("Create a default application");
                application = new Application();
            }
        }

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
        this.server.init(getApplication());

        return this;
    }

    /**
     * Start the web server on this port.
     *
     * @param port
     */
    public void start(int port) {
        getServer().setPort(port);
        start();
    }

    public void start() {
        if (running) {
            log.warn("Server is already started");
            return;
        }

        WebServer server = getServer();
        log.debug("Start server '{}'", server);
        server.start();
        running = true;

        if (reloading) {
            startReloadWatcher();
        }
    }

    public void stop() {
        if (!running) {
            log.debug("Server is not started");
            return;
        }

        if (reloading) {
            stopReloadWatcher();
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

    public Pippo setFilterPath(String filterPath) {
        getServer().setPippoFilterPath(filterPath);

        return this;
    }

    /**
     * Create a pippo instance, add a route on "/" that responds with a message.
     *
     * @param text
     * @return
     */
    public static Pippo send(final String text) {
        Pippo pippo = new Pippo();
        pippo.GET("/", routeContext -> routeContext.send(text));
        pippo.start();

        return pippo;
    }

    protected void startReloadWatcher() {
        if (reloadWatcher == null) {
            reloadWatcher = createReloadWatcher();
        }
        reloadWatcher.start();
    }

    protected void stopReloadWatcher() {
        if (reloadWatcher != null) {
            reloadWatcher.stop();
            reloadWatcher = null;
        }
    }

    protected ReloadWatcher createReloadWatcher() {
        return new ReloadWatcher.Builder()
            .addDirectory(System.getProperty(PippoConstants.SYSTEM_PROPERTY_RELOAD_TARGET_CLASSES, "target/classes"))
            .build(this);
    }

    @Override
    public void onEvent(ReloadWatcher.Event event, Path dir, Path path) {
        stop();

        // TODO: very important (I cannot delete this block)
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            // ignore
        }

        application = createApplication();
        getServer().getPippoFilter().setApplication(application);

        start();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(Pippo.this::stop));
    }

    private Application createApplication() {
        String targetClasses = System.getProperty(PippoConstants.SYSTEM_PROPERTY_RELOAD_TARGET_CLASSES, "target/classes");
        log.debug("Target classes is '{}'", targetClasses);
        String rootPackageName = System.getProperty(PippoConstants.SYSTEM_PROPERTY_RELOAD_ROOT_PACKAGE_NAME, "");
        log.debug("Root package name is '{}'", rootPackageName);
        ClassLoader classLoader = new ReloadClassLoader(Pippo.class.getClassLoader(), rootPackageName) {

            @Override
            protected InputStream getInputStream(String path) {
                Path resolvedPath = Paths.get(targetClasses).resolve(path);
                if (Files.notExists(resolvedPath)) {
                    return null;
                }

                try {
                    return Files.newInputStream(resolvedPath);
                } catch (IOException e) {
                    throw new PippoRuntimeException(e);
                }
            }

        };

        Application application = ServiceLocator.locate(Application.class, classLoader);
        if (application != null) {
            return application;
        }

        String applicationClassName = System.getProperty(PippoConstants.SYSTEM_PROPERTY_APPLICATION_CLASS_NAME);
        if (applicationClassName == null) {
            throw new PippoRuntimeException("Cannot find the application class name");
        }
        log.debug("Application class name is '{}'", applicationClassName);

        try {
            Class<?> applicationClass = classLoader.loadClass(applicationClassName);
            application = (Application) applicationClass.newInstance();
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return application;
    }

    private void initReloading() {
        String reloadEnabled = System.getProperty(PippoConstants.SYSTEM_PROPERTY_RELOAD_ENABLED);
        if (reloadEnabled != null) {
            reloading = Boolean.valueOf(reloadEnabled);
        } else {
            reloading = RuntimeMode.DEV == RuntimeMode.getCurrent();
        }
    }

}
