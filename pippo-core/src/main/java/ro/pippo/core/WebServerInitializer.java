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

import javax.servlet.ServletContext;

/**
 * Interface to be implemented in Servlet 3.0+ environments in order to configure the
 * {@link ServletContext} programmatically.
 * This interface is used by {@link PippoServletContextListener} so DON'T forget to annotate the
 * implementations with {@code @MetaInfServices}.
 *
 * @author Decebal Suiu
 */
public interface WebServerInitializer {

    /**
     * This will be invoked before any of the filters and servlets are initialized.
     * It acquires the reference to {@code ServletContext} and makes use of the methods addServlet(),
     * addMapping(), setInitParameters() and addFilter() for dynamically adding web components.
     *
     * @param servletContext
     */
    void init(ServletContext servletContext);

    /**
     * This will be invoked after all the servlets and filters have been destroyed.
     *
     * @param servletContext
     */
    void destroy(ServletContext servletContext);

}
