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

import java.util.EventListener;

/**
 * @author Decebal Suiu
 */
public interface WebServer<T extends WebServerSettings> {

    /**
     * Attribute name used to retrieve the application instance from a {@link javax.servlet.ServletContext}.
     * See also {@link WebServerInitializer}.
     *
     * <pre>
     * {@code
     * MyApplication application = (MyApplication) servletContext.getAttribute(PIPPO_APPLICATION);
     * }
     * </pre>
     *
     * A possible scenario: I want to add support for Jersey in my application.
     * <pre>
     * {@code
     *
     * public class MyApplication extends Application {
     *
     *     public ResourceConfig getResourceConfig() {
     *         return new ResourceConfig(MyResource.class);
     *     }
     *
     *     // other possible methods
     *
     * }
     *
     * @MetaInfServices
     * public class JerseyInitializer implements WebServerInitializer {
     *
     *     @Override
     *     public void init(ServletContext servletContext) {
     *         // get the resourceConfig via application
     *         MyApplication application = (MyApplication) servletContext.getAttribute(PIPPO_APPLICATION);
     *         ResourceConfig resourceConfig = application.getResourceConfig();
     *
     *         // add jersey filter
     *         ServletRegistration.Dynamic jerseyServlet = servletContext.addServlet("jersey", new ServletContainer(resourceConfig));
     *         jerseyServlet.setLoadOnStartup(1);
     *         jerseyServlet.addMapping("/api/*");
     *     }
     *
     *     @Override
     *     public void destroy(ServletContext servletContext) {
     *         // do nothing for now
     *     }
     *
     * }
     * </pre>
     */
    public static final String PIPPO_APPLICATION = "PIPPO_APPLICATION";

    T getSettings();

    PippoFilter getPippoFilter();

    WebServer<T> setPippoFilter(PippoFilter pippoFilter);

    String getPippoFilterPath();

    /**
     * The <code>pippoFilterPath</code> must start with <code>"/"</code> and end with <code>"/*"</code>.
     * For example: <code>/*, /app/*</code>
     *
     * @param pippoFilterPath
     * @return
     */
    WebServer<T> setPippoFilterPath(String pippoFilterPath);

    WebServer<T> init(Application application);

    void start();

    void stop();

    /**
     * Add an {@link EventListener} programmatically.
     *
     * Servlet API provides following Listener interfaces:
     *
     * <ul>
     * <li>{@code javax.servlet.ServletContextListener}</li>
     * <li>{@code javax.servlet.ServletContextAttributeListener}</li>
     * <li>{@code javax.servlet.ServletRequestListener}</li>
     * <li>{@code javax.servlet.ServletRequestAttributeListener}</li>
     * <li>{@code javax.servlet.http.HttpSessionListener}</li>
     * <li>{@code javax.servlet.http.HttpSessionBindingListener}</li>
     * <li>{@code javax.servlet.http.HttpSessionAttributeListener}</li>
     * <li>{@code javax.servlet.http.HttpSessionActivationListener}</li>
     * <li>{@code javax.servlet.AsyncListener}</li>
     * </ul>
     *
     * @param listener
     * @return
     */
    WebServer addListener(Class<? extends EventListener> listener);

    default int getPort() {
        return getSettings().getPort();
    }

    default WebServer<T> setPort(int port) {
        getSettings().port(port);

        return this;
    }

}
