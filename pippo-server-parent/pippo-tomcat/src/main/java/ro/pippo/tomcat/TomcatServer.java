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
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoServlet;
import ro.pippo.core.PippoServletContextListener;
import ro.pippo.core.WebServer;
import ro.pippo.core.util.StringUtils;

import java.io.File;

/**
 * @author Daniel Jipa
 */
@MetaInfServices(WebServer.class)
public class TomcatServer extends AbstractWebServer<TomcatSettings> {

    private static final Logger log = LoggerFactory.getLogger(TomcatServer.class);

    private Tomcat tomcat;

    @Override
    public void start() {
        if (StringUtils.isNullOrEmpty(pippoFilterPath)) {
            pippoFilterPath = "/*";
        }

        tomcat = createTomcat();
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
        pippoServlet.setApplication(getApplication());

        Wrapper wrapper = context.createWrapper();
        String name = "pippoServlet";

        wrapper.setName(name);
        wrapper.setLoadOnStartup(1);
        wrapper.setServlet(pippoServlet);
        wrapper.setMultipartConfigElement(createMultipartConfigElement());
        wrapper.addMapping(pippoFilterPath);
        context.addChild(wrapper);

        // inject application as context attribute
        context.getServletContext().setAttribute(PIPPO_APPLICATION, getApplication());

        // add initializers
        context.addApplicationListener(PippoServletContextListener.class.getName());

        // add listeners
        listeners.forEach(listener -> context.addApplicationListener(listener.getName()));

        String version = tomcat.getClass().getPackage().getImplementationVersion();
        log.info("Starting Tomcat Server {} on port {}", version, getSettings().getPort());

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            log.error("Unable to launch Tomcat", e);
            throw new PippoRuntimeException(e);
        }

        if (!getApplication().getPippoSettings().isTest()) {
            tomcat.getServer().await();
        }
    }

    @Override
    public void stop() {
        if (tomcat != null) {
            try {
                tomcat.stop();
            } catch (Exception e) {
                throw new PippoRuntimeException(e, "Cannot stop Tomcat Server");
            }
        }
    }

    @Override
    protected TomcatSettings createDefaultSettings() {
        return new TomcatSettings(getApplication().getPippoSettings());
    }

    protected Tomcat createTomcat() {
        return new Tomcat();
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
        connector.setProperty("keyAlias", getSettings().getKeyAlias());
        connector.setProperty("keystorePass", getSettings().getKeystorePassword());
        connector.setProperty("keystoreType", getSettings().getKeyType());
        connector.setProperty("keystoreFile", getSettings().getKeystoreFile());
        connector.setProperty("clientAuth", getSettings().getClientAuth() ? "required" : "none");
        if (getSettings().getClientAuth()) {
            connector.setProperty("truststoreFile", getSettings().getTruststoreFile());
            connector.setProperty("truststorePassword", getSettings().getTruststorePassword());
        }
        connector.setProperty("protocol", "HTTP/1.1");
        connector.setProperty("sslProtocol", "TLS");
        connector.setProperty("maxThreads", String.valueOf(getSettings().getMaxConnections()));
        connector.setProperty("SSLEnabled", "true");
    }

}
