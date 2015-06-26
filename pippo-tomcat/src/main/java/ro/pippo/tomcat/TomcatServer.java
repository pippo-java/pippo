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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.AbstractWebServer;
import ro.pippo.core.Application;
import ro.pippo.core.PippoFilter;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoServlet;
import ro.pippo.core.util.StringUtils;

import java.io.File;

/**
 * @author Daniel Jipa
 */
public class TomcatServer extends AbstractWebServer<TomcatSettings> {

    private static final Logger log = LoggerFactory.getLogger(TomcatServer.class);

    private Application application;

    private Tomcat tomcat;

    @Override
    public void setPippoFilter(PippoFilter pippoFilter) {
        super.setPippoFilter(pippoFilter);
        application = pippoFilter.getApplication();
    }

    @Override
    public void start() {
        if (StringUtils.isNullOrEmpty(pippoFilterPath)) {
            pippoFilterPath = "/*";
        }
        tomcat = new Tomcat();
        tomcat.setBaseDir(getSettings().getBaseFolder());
        if (getSettings().getKeystoreFile() == null) {
            enablePlainConnector(tomcat);
        }else{
            enableSSLConnector(tomcat);
        }
        File docBase = new File(System.getProperty("java.io.tmpdir"));
        Context context = tomcat.addContext(getSettings().getContextPath(), docBase.getAbsolutePath());

        PippoServlet pippoServlet = new PippoServlet();
        pippoServlet.setApplication(application);

        Wrapper wrapper = context.createWrapper();
        String name = "pippoServlet";

        wrapper.setName(name);
        wrapper.setLoadOnStartup(1);
        wrapper.setServlet(pippoServlet);
        context.addChild(wrapper);
        context.addServletMapping(pippoFilterPath, name);

        try {
            String version = tomcat.getClass().getPackage().getImplementationVersion();
            log.info("Starting Tomcat Server {} on port {}", version, getSettings().getPort());
            tomcat.start();
        } catch (LifecycleException e) {
            throw new PippoRuntimeException(e);
        }
        tomcat.getServer().await();
    }

    private void enablePlainConnector(Tomcat tomcat){
        log.info("Using http protocol");
        tomcat.setPort(getSettings().getPort());
    }


    private void enableSSLConnector(Tomcat tomcat){
        log.info("Using https protocol");
        Connector connector = tomcat.getConnector();
        connector.setPort(getSettings().getPort());
        connector.setSecure(true);
        connector.setScheme("https");
        connector.setAttribute("keyAlias", getSettings().getKeyAlias());
        connector.setAttribute("keystorePass", getSettings().getKeystorePassword());
        connector.setAttribute("keystoreType", getSettings().getKeyType());
        connector.setAttribute("keystoreFile",
            getSettings().getKeystoreFile());
        connector.setAttribute("clientAuth", "false");
        connector.setAttribute("protocol", "HTTP/1.1");
        connector.setAttribute("sslProtocol", "TLS");
        connector.setAttribute("maxThreads", getSettings().getMaxConnections());
        connector.setAttribute("protocol", "org.apache.coyote.http11.Http11AprProtocol");
        connector.setAttribute("SSLEnabled", true);
    }

    @Override
    public void stop() {
        if (tomcat != null) {
            try {
                tomcat.stop();
            } catch (Exception e) {
                throw new PippoRuntimeException("Cannot stop Tomcat Server", e);
            }
        }
    }

    @Override
    protected TomcatSettings createDefaultSettings() {
        return new TomcatSettings(pippoSettings);
    }

}
