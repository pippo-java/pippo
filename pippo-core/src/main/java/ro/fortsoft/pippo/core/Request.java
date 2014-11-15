/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pippo.core.util.IoUtils;
import ro.fortsoft.pippo.core.util.StringValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a server-side HTTP request. An instance of this class is created for each request.
 *
 * @author Decebal Suiu
 */
public class Request {

    private static final Logger log = LoggerFactory.getLogger(Request.class);

    private HttpServletRequest httpServletRequest;
    private Map<String, StringValue> parameters;
    private Map<String, String> pathParameters;
    private Map<String, StringValue> allParameters; // parameters + pathParameters
    private Map<String, FileItem> files;
    private Session session;

    private String body; // cache

    Request(HttpServletRequest servletRequest) {
        this.httpServletRequest = servletRequest;

        // fill (query) parameters if any
        Map<String, StringValue> tmp = new HashMap<String, StringValue>();
        Enumeration<String> names = httpServletRequest.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            tmp.put(name, new StringValue(httpServletRequest.getParameter(name)));
        }
        parameters = Collections.unmodifiableMap(tmp);
    }

    public Map<String, StringValue> getParameters() {
        return getAllParameters();
    }

    public StringValue getParameter(String name) {
        if (!getAllParameters().containsKey(name)) {
            return new StringValue(null);
        }

        return getAllParameters().get(name);
    }

    public <T> T getEntityFromParameters(Class<T> entityClass) {
        T entity;

        try {
            entity = entityClass.newInstance();
        } catch (Exception e) {
            log.error("Cannot create new instance of class '{}'", entityClass.getName(), e);
            return null;
        }

        for (Field field : entityClass.getDeclaredFields()) {
            if (getAllParameters().containsKey(field.getName())) {
                field.setAccessible(true);
                try {
                    Object value = getAllParameters().get(field.getName()).to(field.getType());
                    field.set(entity, value);
                } catch (IllegalAccessException e) {
                    log.error("Cannot set value for field '{}'", field.getName(), e);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        return entity;
    }

    public String getHost() {
        return httpServletRequest.getHeader(HttpConstants.Header.HOST);
    }

    public String getUserAgent() {
        return httpServletRequest.getHeader(HttpConstants.Header.USER_AGENT);
    }

    public int getPort() {
        return httpServletRequest.getServerPort();
    }

    public String getClientIp() {
        return httpServletRequest.getRemoteAddr();
    }

    public int getContentLength() {
        return httpServletRequest.getContentLength();
    }

    public String getScheme() {
        return httpServletRequest.getScheme();
    }

    public String getBody() {
        if (body == null) {
            try {
                body = IoUtils.toString(httpServletRequest.getInputStream());
            } catch (Exception e) {
                throw new PippoRuntimeException("Exception when reading the request body", e);
            }
        }

        return body;
    }

    public String getHeader(String name) {
        return httpServletRequest.getHeader(name);
    }

    public boolean isSecure() {
        return httpServletRequest.isSecure();
    }

    public String getUrl() {
        return httpServletRequest.getRequestURL().toString();
    }

    public String getUri() {
        return httpServletRequest.getRequestURI();
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public Session getSession() {
        return getSession(true);
    }

    public Session getSession(boolean create) {
        if (session == null) {
            HttpSession httpSession = httpServletRequest.getSession(create);
            if (httpSession != null) {
                session = new Session(httpSession);
            }
        }

        return session;
    }

    public Map<String, FileItem> getFiles() {
        if (files == null) {
            files = new HashMap<String, FileItem>();
            try {
                Collection<Part> parts = httpServletRequest.getParts();
                for (Part part : parts) {
                    files.put(part.getName(), new FileItem(part));
                }
            } catch (Exception e) {
                throw new PippoRuntimeException("Cannot get files", e);
            }
        }

        return files;
    }

    public FileItem getFile(String name) {
        return getFiles().get(name);
    }

    // called in (Default)RouteHandlerChain.next()
    public void setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
        allParameters = null; // invalidate and force recreate
    }

    private Map<String, StringValue> getAllParameters() {
        if (allParameters == null) {
            Map<String, StringValue> tmp = new HashMap<String, StringValue>();
            // add query parameters
            tmp.putAll(parameters);
            // add path parameters
            Set<String> names = pathParameters.keySet();
            for (String name : names) {
                tmp.put(name, new StringValue(pathParameters.get(name)));
            }
            allParameters = Collections.unmodifiableMap(tmp);
        }

        return allParameters;
    }

}
