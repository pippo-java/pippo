/*
 * Copyright (C) 2016 the original author or authors.
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
import ro.pippo.core.util.ServiceLocator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;

/**
 * Delegate the {@code ServletContext} to any {@link WebServerInitializer}
 * implementations found via {@link java.util.ServiceLoader}.
 *
 * @author Decebal Suiu
 */
public class PippoServletContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(PippoServletContextListener.class);

    private List<WebServerInitializer> initializers;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        initializers = ServiceLocator.locateAll(WebServerInitializer.class);
        for (WebServerInitializer initializer : initializers) {
            try {
                log.debug("Found initializer '{}'", initializer.getClass().getName());
                initializer.init(sce.getServletContext());
            } catch (Exception e) {
                log.error("Failed to init '{}'", initializer.getClass().getName(), e);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        for (WebServerInitializer initializer : initializers) {
            try {
                log.debug("Found initializer '{}'", initializer.getClass().getName());
                initializer.destroy(sce.getServletContext());
            } catch (Exception e) {
                log.error("Failed to destroy '{}'", initializer.getClass().getName(), e);
            }
        }
    }

}
