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
package ro.pippo.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.AbstractWebServer;
import ro.pippo.core.Application;
import ro.pippo.core.PippoFilter;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoServlet;
import ro.pippo.core.PippoServletContextListener;
import ro.pippo.core.WebServer;
import ro.pippo.core.util.StringUtils;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Daniel Jipa
 */
@MetaInfServices(WebServer.class)
public class TomcatServer extends AbstractWebServer<TomcatSettings> {

    private static final Logger log = LoggerFactory.getLogger(TomcatServer.class);

    private Application application;
    private Tomcat tomcat;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final CountDownLatch startLatch = new CountDownLatch(1);

    @Override
    public void start() {
        executor.submit(this::internalStart);
        try {
            startLatch.await();
        } catch (InterruptedException e) {
            log.info(e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (tomcat != null) {
            try {
                tomcat.stop();
                executor.shutdownNow();
            } catch (Exception e) {
                throw new PippoRuntimeException(e, "Cannot stop Tomcat Server");
            }
        }
    }

    @Override
    public WebServer setPippoFilter(PippoFilter pippoFilter) {
        super.setPippoFilter(pippoFilter);

        application = pippoFilter.getApplication();

        return this;
    }

    @Override
    protected TomcatSettings createDefaultSettings() {
        return new TomcatSettings(pippoSettings);
    }

    protected void internalStart() {
        if (StringUtils.isNullOrEmpty(pippoFilterPath)) {
            pippoFilterPath = "/*";
        }

        tomcat = new Tomcat();
        tomcat.setBaseDir(getSettings().getBaseFolder());

        if (getSettings().getKeystoreFile() == null) {
            enablePlainConnector(tomcat);
        } else {
            enableSSLConnector(tomcat);
        }

        File docBase = new File(System.getProperty("java.io.tmpdir"));
        Context context = tomcat.addContext(getSettings().getContextPath(), docBase.getAbsolutePath());
        context.setAllowCasualMultipartParsing(true);
        PippoServlet pippoServlet = new PippoServlet();
        pippoServlet.setApplication(application);

        Wrapper wrapper = context.createWrapper();
        String name = "pippoServlet";

        wrapper.setName(name);
        wrapper.setLoadOnStartup(1);
        wrapper.setServlet(pippoServlet);
        context.addChild(wrapper);
        context.addServletMapping(pippoFilterPath, name);

        // inject application as context attribute
        context.getServletContext().setAttribute(PIPPO_APPLICATION, application);

        // add initializers
        context.addApplicationListener(PippoServletContextListener.class.getName());

        // add listeners
        listeners.forEach(listener -> context.addApplicationListener(listener.getName()));

        try {
            String version = tomcat.getClass().getPackage().getImplementationVersion();
            log.info("Starting Tomcat Server {} on port {}", version, getSettings().getPort());
            tomcat.start();
        } catch (LifecycleException e) {
            throw new PippoRuntimeException(e);
        }

        startLatch.countDown();
        tomcat.getServer().await();
    }

    private void enablePlainConnector(Tomcat tomcat) {
        log.info("Using http protocol");
        tomcat.setPort(getSettings().getPort());
    }

    private void enableSSLConnector(Tomcat tomcat) {
        log.info("Using https protocol");
        Connector connector = tomcat.getConnector();
        connector.setPort(getSettings().getPort());
        connector.setSecure(true);
        connector.setScheme("https");
        connector.setAttribute("keyAlias", getSettings().getKeyAlias());
        connector.setAttribute("keystorePass", getSettings().getKeystorePassword());
        connector.setAttribute("keystoreType", getSettings().getKeyType());
        connector.setAttribute("keystoreFile", getSettings().getKeystoreFile());
        connector.setAttribute("clientAuth", getSettings().getClientAuth());
        if (getSettings().getClientAuth()) {
            connector.setAttribute("truststoreFile", getSettings().getTruststoreFile());
            connector.setAttribute("truststorePass", getSettings().getTruststorePassword());
        }
        connector.setAttribute("protocol", "HTTP/1.1");
        connector.setAttribute("sslProtocol", "TLS");
        connector.setAttribute("maxThreads", getSettings().getMaxConnections());
        connector.setAttribute("protocol", "org.apache.coyote.http11.Http11AprProtocol");
        connector.setAttribute("SSLEnabled", true);
    }

}
