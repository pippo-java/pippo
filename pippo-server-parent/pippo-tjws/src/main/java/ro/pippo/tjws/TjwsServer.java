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
package ro.pippo.tjws;

import Acme.Serve.Serve;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.AbstractWebServer;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoServlet;
import ro.pippo.core.WebServer;
import ro.pippo.core.WebServerSettings;

import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * RESTEasy TJWS is a 100KB Servlet 2.5 container.
 * <p/>
 * Not all Pippo features are available in Servlet 2.5, specifically
 * file uploading and servlet filters.
 * <p/>
 * TjwsServer uses PippoServlet.
 *
 * TjwsServer doesn't support {@link ServletContextListener}.
 *
 * @author James Moger
 */
@MetaInfServices(WebServer.class)
public class TjwsServer extends AbstractWebServer<WebServerSettings> {

    private static final Logger log = LoggerFactory.getLogger(TjwsServer.class);

    private Serve server;
    private PippoServlet pippoServlet;

    @Override
    public void start() {
        pippoServlet = new PippoServlet();
        pippoServlet.setApplication(getApplication());

        Map<String, Object> arguments = new HashMap<>();
        arguments.put(Serve.ARG_BINDADDRESS, getSettings().getHost());
        arguments.put(Serve.ARG_PORT, getSettings().getPort());

        String version = "";
        URL pomUrl = Serve.class.getResource("/META-INF/maven/org.jboss.resteasy/tjws/pom.properties");
        try (InputStream is = pomUrl.openStream()) {
            Properties props = new Properties();
            props.load(is);
            version = props.getProperty("version");
        } catch (IOException e) {
            log.error("Failed to read RESTEasy TJWS pom.properties!", e);
        }

        log.info("Starting RESTEasy TJWS Server {} on port {}", version, getSettings().getPort());
        server = new Serve(arguments, System.err);
        server.addServlet("/", pippoServlet);
        server.runInBackground();
    }

    @Override
    public void stop() {
        if (server != null) {
            try {
                server.stopBackground();
                // We must manually destroy PippoServlet because
                // TJWS does not destroy the root servlet. :(
                pippoServlet.destroy();
            } catch (Exception e) {
                throw new PippoRuntimeException(e, "Cannot stop RESTEasy TJWS Server");
            }
        }
    }

    @Override
    public WebServer<WebServerSettings> addListener(Class<? extends EventListener> listener) {
        throw new PippoRuntimeException("This feature is not available for this server type");
    }

    @Override
    protected WebServerSettings createDefaultSettings() {
        return new WebServerSettings(getApplication().getPippoSettings());
    }

}
