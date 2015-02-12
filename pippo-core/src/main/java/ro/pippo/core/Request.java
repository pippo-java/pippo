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
import ro.pippo.core.session.Session;
import ro.pippo.core.session.SessionFactory;
import ro.pippo.core.util.IoUtils;
import ro.pippo.core.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a server-side HTTP request. An instance of this class is created
 * for each request.
 *
 * @author Decebal Suiu
 */
public final class Request {

    private static final Logger log = LoggerFactory.getLogger(Request.class);

    private HttpServletRequest httpServletRequest;
    private ContentTypeEngines contentTypeEngines;
    private SessionFactory sessionFactory;
    private Map<String, ParameterValue> parameters;
    private Map<String, String> pathParameters;
    private Map<String, ParameterValue> allParameters; // parameters + pathParameters
    private Map<String, FileItem> files;
    private Session session;
    private String contextPath;

    private String body; // cache

    public Request(HttpServletRequest servletRequest, Application application) {
        this.httpServletRequest = servletRequest;
        this.contentTypeEngines = application.getContentTypeEngines();
        this.sessionFactory = application.getSessionFactory();

        // fill (query) parameters if any
        Map<String, ParameterValue> tmp = new HashMap<>();
        Enumeration<String> names = httpServletRequest.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String[] values = httpServletRequest.getParameterValues(name);
            tmp.put(name, new ParameterValue(values));
        }
        parameters = Collections.unmodifiableMap(tmp);
        contextPath = application.getRouter().getContextPath();
    }

    public Map<String, ParameterValue> getParameters() {
        return getAllParameters();
    }

    public ParameterValue getParameter(String name) {
        if (!getAllParameters().containsKey(name)) {
            return new ParameterValue();
        }

        return getAllParameters().get(name);
    }

    public <T> T createEntityFromParameters(Class<T> entityClass) {
        T entity;
        try {
            entity = entityClass.newInstance();
        } catch (Exception e) {
            log.error("Cannot create new instance of class '{}'", entityClass.getName(), e);
            return null;
        }

        updateEntityFromParameters(entity);

        return entity;
    }

    public <T> T updateEntityFromParameters(T entity) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            String parameterName = field.getName();
            Param parameter = field.getAnnotation(Param.class);
            if (parameter != null) {
                parameterName = parameter.value();
            }

            if (getAllParameters().containsKey(parameterName)) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                String pattern = null;
                ParamPattern parameterPattern = field.getAnnotation(ParamPattern.class);
                if (parameterPattern != null) {
                    pattern = parameterPattern.value();
                }

                try {
                    Object value = getAllParameters().get(parameterName).to(field.getType(), pattern);
                    field.set(entity, value);
                } catch (IllegalAccessException e) {
                    log.error("Cannot set value for field '{}' from parameter '{}'", field.getName(), parameterName, e);
                } catch (PippoRuntimeException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        return entity;
    }

    public <T> T createEntityFromBody(Class<T> entityClass) {
        T entity = null;
        try {
            String body = getBody();
            if (StringUtils.isNullOrEmpty(body)) {
                log.warn("Can not create entity '{}' from null or empty request body!", entityClass.getName());
                return null;
            }

            // try to determine the body content-type
            String contentType = getContentType();
            if (StringUtils.isNullOrEmpty(contentType)) {
                // sloppy client, try the accept header
                contentType = getAcceptType();
            }

            if (StringUtils.isNullOrEmpty(contentType)) {
                throw new PippoRuntimeException(
                    "Failed to create entity '{}' from request body because 'content-type' is not specified!",
                    entityClass.getName());
            }

            ContentTypeEngine engine = contentTypeEngines.getContentTypeEngine(contentType);
            if (engine == null) {
                throw new PippoRuntimeException(
                    "Failed to create entity '{}' from request body because a content engine for '{}' could not be found!",
                    entityClass.getName(), contentType);
            }

            entity = engine.fromString(body, entityClass);

        } catch (PippoRuntimeException e) {
            // pass-through PippoRuntimeExceptions
            throw e;
        } catch (Exception e) {
            // capture and re-throw all other exceptions
            throw new PippoRuntimeException("Failed to create entity '{}' from request body!", e, entityClass.getName());
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

    public String getAcceptType() {
        return httpServletRequest.getHeader(HttpConstants.Header.ACCEPT);
    }

    public String getContentType() {
        return httpServletRequest.getHeader(HttpConstants.Header.CONTENT_TYPE);
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

    /**
     * Returns the url with the protocol, context path, & resource path. The
     * query string is omitted.
     *
     * @return the url
     */
    public String getUrl() {
        return httpServletRequest.getRequestURL().toString();
    }

    /**
     * Returns the complete url with the protocol, context path, resource path, and
     * query string.
     *
     * @return the complete url
     */
    public String getUrlWithQuery() {
        StringBuilder sb = new StringBuilder(getUrl());
        if (getQuery() != null) {
            sb.append('?').append(getQuery());
        }
        return sb.toString();
    }

    /**
     * Returns the container uri with the context path & resource path. The
     * protocol and query string are omitted.
     *
     * @return the container uri
     */
    public String getUri() {
        return httpServletRequest.getRequestURI();
    }

    /**
     * Returns the uri relative to the context.
     *
     * @return the uri relative to the context
     */
    public String getContextUri() {
        if ("".equals(contextPath)) {
            return getUri();
        } else {
            return getUri().substring(contextPath.length());
        }
    }

    /**
     * Returns the query string component of the request.
     *
     * @return the query string
     */
    public String getQuery() {
        return httpServletRequest.getQueryString();
    }

    /**
     * Returns the uri relative to the servlet container with the context path,
     * resource path, and query string. The protocol is omitted.
     *
     * @return the container uri with the query
     */
    public String getUriWithQuery() {
        StringBuilder sb = new StringBuilder(getUri());
        if (getQuery() != null) {
            sb.append('?').append(getQuery());
        }
        return sb.toString();
    }

    /**
     * Returns the uri with the query string relative to the context.
     *
     * @return the context-relative uri with the query
     */
    public String getContextUriWithQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append(getContextUri());
        if (getQuery() != null) {
            sb.append('?').append(getQuery());
        }
        return sb.toString();
    }

    public String getMethod() {
        return httpServletRequest.getMethod();
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public Session getSession() {
        return getSession(true);
    }

    public Session getSession(boolean create) {
        /*
        if (create && session == null) {
            session = sessionFactory.getSession(this);
        }
        */
        if (session == null) {
            session = sessionFactory.getSession(this, create);
        }

        return session;
    }

    public void resetSession() {
        if (session == null) {
            return;
        }
        session.invalidate();
        session = null;
        getSession();
    }

    public void recreateSession() {
        if (session == null) {
            return;
        }

        // preserve the session data
        Map<String, Object> values = new HashMap<>();
        Enumeration<String> keys = getSession().getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            values.put(key, getSession().get(key));
        }

        // preserve the flash data
        Flash flash = session.getFlash();

        // create a new session
        resetSession();

        // restore the session data
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            getSession().put(entry.getKey(), entry.getValue());
        }

        // restore the flash instance
        if (flash != null) {
            getSession().put("flash", flash);
        }
    }

    public Map<String, FileItem> getFiles() {
        if (files == null) {
            files = new HashMap<>();
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

    private Map<String, ParameterValue> getAllParameters() {
        if (allParameters == null) {
            Map<String, ParameterValue> tmp = new HashMap<>();
            // add query parameters
            tmp.putAll(parameters);

            // add path parameters
            if (pathParameters != null) {
                Set<String> names = pathParameters.keySet();
                for (String name : names) {
                    tmp.put(name, new ParameterValue(pathParameters.get(name)));
                }
            }
            allParameters = Collections.unmodifiableMap(tmp);
        }

        return allParameters;
    }

    public List<Cookie> getCookies() {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(cookies);
    }

    public Cookie getCookie(String name) {
        for (Cookie cookie : getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Request{" +
            "requestMethod='" + getMethod() + '\'' +
            ", uriPattern='" + getContextUri() + '\'' +
            '}';
    }

}
