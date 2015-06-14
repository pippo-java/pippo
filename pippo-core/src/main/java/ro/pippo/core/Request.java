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
import ro.pippo.core.route.RouteDispatcher;
import ro.pippo.core.util.CookieUtils;
import ro.pippo.core.util.IoUtils;
import ro.pippo.core.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
    private Map<String, ParameterValue> parameters;
    private Map<String, String> pathParameters;
    private Map<String, ParameterValue> allParameters; // parameters + pathParameters
    private Map<String, ParameterValue> allPathParameters; // path parameters
    private Map<String, ParameterValue> allQueryParameters; // query parameters
    private Map<String, FileItem> files;
    private Session session;
    private String applicationPath;
    private String method;
    private String path;

    private String contentType;
    private String body; // cache

    public Request(HttpServletRequest servletRequest, Application application) {
        this.httpServletRequest = servletRequest;
        this.contentTypeEngines = application.getContentTypeEngines();

        // fill (query) parameters if any
        Map<String, Map<Integer, String>> arrays = new HashMap<>();
        Map<String, ParameterValue> tmp = new HashMap<>();
        Enumeration<String> names = httpServletRequest.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();

            if (name.matches("(.+)\\[(\\d+)\\]")) {
                // support indexed parameter arrays e.g. setting[0], setting[1], setting[2]
                int brk = name.indexOf('[');
                String base = name.substring(0, brk);
                int idx = Integer.parseInt(name.substring(brk + 1, name.length() - 1));
                if (!arrays.containsKey(base)) {
                    // use an ordered map because we can not rely on parameter
                    // order from the servlet container nor from the request
                    arrays.put(base, new TreeMap<Integer, String>());
                }

                Map<Integer, String> map = arrays.get(base);
                String value = httpServletRequest.getParameterValues(name)[0];
                map.put(idx, value);
            } else {
                String[] values = httpServletRequest.getParameterValues(name);
                tmp.put(name, new ParameterValue(values));
            }
        }

        for (Map.Entry<String, Map<Integer, String>> entry : arrays.entrySet()) {
            // identify maximum specified index
            int maxIndex = 0;
            for (int index : entry.getValue().keySet()) {
                if (index > maxIndex) {
                    maxIndex = index;
                }
            }

            // populate array & respect specified indexes
            // Note: this may not be linear but we must respect that design choice
            String[] values = new String[maxIndex + 1];
            for (Map.Entry<Integer, String> indexedValue : entry.getValue().entrySet()) {
                values[indexedValue.getKey()] = indexedValue.getValue();
            }
            tmp.put(entry.getKey(), new ParameterValue(values));
        }

        parameters = Collections.unmodifiableMap(tmp);
        applicationPath = application.getRouter().getApplicationPath();
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

    /**
     * Return all query parameters
     */
    public Map<String, ParameterValue> getQueryParameters() {
        return getAllQueryParameters();
    }

    /**
     * Return one query parameter
     */
    public ParameterValue getQueryParameter(String name) {
        if (!getAllQueryParameters().containsKey(name)) {
            return new ParameterValue();
        }
        return getAllQueryParameters().get(name);
    }

    /**
     * Return all path parameters
     */
    public Map<String, ParameterValue> getPathParameters() {
        return getAllPathParameters();
    }

    /**
     * Return one path parameter
     */
    public ParameterValue getPathParameter(String name) {
        if (!getAllPathParameters().containsKey(name)) {
            return new ParameterValue();
        }
        return getAllPathParameters().get(name);
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

    public <T, X> T updateEntityFromParameters(T entity) {
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
                    Class<?> fieldClass = field.getType();
                    Object value;
                    if (Collection.class.isAssignableFrom(fieldClass)) {
                        Type parameterType = field.getGenericType();
                        if (!ParameterizedType.class.isAssignableFrom(parameterType.getClass())) {
                            String msg = "Please specify a generic parameter type for field '{}' {}";
                            throw new PippoRuntimeException(msg, field.getName(), fieldClass.getName());
                        }
                        ParameterizedType parameterizedType = (ParameterizedType) parameterType;
                        Class<X> genericClass = null;
                        try {
                            genericClass = (Class<X>) parameterizedType.getActualTypeArguments()[0];
                        } catch (ClassCastException e) {
                            String msg = "Please specify a generic parameter type for field '{}' {}";
                            throw new PippoRuntimeException(msg, field.getName(), fieldClass.getName());
                        }

                        if (Set.class == fieldClass) {
                            value = getAllParameters().get(parameterName).toSet(genericClass, pattern);
                        } else if (List.class == fieldClass) {
                            value = getAllParameters().get(parameterName).toList(genericClass, pattern);
                        } else if (fieldClass.isInterface()) {
                            String msg = "Field '{}' collection '{}' is not a supported type!";
                            throw new PippoRuntimeException(msg, field.getName(), fieldClass.getName());
                        } else {
                            Class<? extends Collection> collectionClass = (Class<? extends Collection>) fieldClass;
                            value = getAllParameters().get(parameterName).toCollection(collectionClass, genericClass, pattern);
                        }
                    } else {
                        value = getAllParameters().get(parameterName).to(fieldClass, pattern);
                    }
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

            return engine.fromString(body, entityClass);
        } catch (PippoRuntimeException e) {
            // pass-through PippoRuntimeExceptions
            throw e;
        } catch (Exception e) {
            // capture and re-throw all other exceptions
            throw new PippoRuntimeException("Failed to create entity '{}' from request body!", e, entityClass.getName());
        }
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
        if (contentType == null) {
            String httpServletRequestContentType = httpServletRequest.getHeader(HttpConstants.Header.CONTENT_TYPE);
            if (HttpConstants.Method.POST.equals(httpServletRequest.getMethod())
                && (HttpConstants.ContentType.APPLICATION_FORM_URLENCODED.equals(httpServletRequestContentType)
                || HttpConstants.ContentType.MULTIPART_FORM_DATA.equals(httpServletRequestContentType))) {
                // Allow forms to exercise RESTful API endpoints by POSTing content like 'application/json'.
                // This parameter is usually paired with '_method' and '_content' parameters.
                contentType = getParameter("_content_type").toString(httpServletRequestContentType);
            } else {
                contentType = httpServletRequestContentType;
            }
        }
        return contentType;
    }

    public String getBody() {
        if (body == null) {
            String httpServletRequestContentType = httpServletRequest.getHeader(HttpConstants.Header.CONTENT_TYPE);
            if (HttpConstants.Method.POST.equals(httpServletRequest.getMethod())
                && (HttpConstants.ContentType.APPLICATION_FORM_URLENCODED.equals(httpServletRequestContentType)
                || HttpConstants.ContentType.MULTIPART_FORM_DATA.equals(httpServletRequestContentType))) {
                // Allow forms to exercise RESTful API endpoints by POSTing content like 'application/json'.
                // This parameter is usually paired with '_method' and '_content_type' parameters.
                body = getParameter("_content").toString(null);
            } else {
                try {
                    body = IoUtils.toString(httpServletRequest.getInputStream());
                } catch (Exception e) {
                    throw new PippoRuntimeException("Exception when reading the request body", e);
                }
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
     * Returns the url with the protocol, application path, & resource path. The
     * query string is omitted.
     *
     * @return the url
     */
    public String getUrl() {
        return httpServletRequest.getRequestURL().toString();
    }

    /**
     * Returns the container uri with the application path & resource path. The
     * protocol and query string are omitted.
     *
     * @return the container uri
     */
    public String getUri() {
        return httpServletRequest.getRequestURI();
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
     * Returns the uri relative to the application path.
     *
     * @return the uri relative to the application path
     */
    public String getApplicationUri() {
        if ("".equals(applicationPath)) {
            return getUri();
        } else {
            return getUri().substring(applicationPath.length());
        }
    }

    /**
     * Returns the uri with the query string relative to the application root path.
     *
     * @return the application-relative uri with the query
     */
    public String getApplicationUriWithQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append(getApplicationUri());
        if (getQuery() != null) {
            sb.append('?').append(getQuery());
        }

        return sb.toString();
    }

    public String getContextPath() {
        return httpServletRequest.getContextPath();
    }

    public String getApplicationPath() {
        return applicationPath;
    }

    /**
     * Returns a request path relative to the application path. The returned value always start with "/".
     */
    public String getPath() {
        if (path == null) {
            // create a URI to automatically decode the path
            URI uri = URI.create(httpServletRequest.getRequestURL().toString());
            String requestUri = uri.getPath();

            path = applicationPath.isEmpty() ? requestUri : requestUri.substring(applicationPath.length());
            if (StringUtils.isNullOrEmpty(path)) {
                path = "/";
            }
        }

        return path;
    }

    public String getMethod() {
        if (method == null) {
            String httpServletRequestMethod = httpServletRequest.getMethod();
            String httpServletRequestContentType = httpServletRequest.getHeader(HttpConstants.Header.CONTENT_TYPE);
            if (HttpConstants.Method.POST.equals(httpServletRequestMethod)
                && (HttpConstants.ContentType.APPLICATION_FORM_URLENCODED.equals(httpServletRequestContentType)
                || HttpConstants.ContentType.MULTIPART_FORM_DATA.equals(httpServletRequestContentType))) {
                // Allow forms to more discretely control the Pippo form handler and encourages RESTful design.
                // This parameter may be paired with the '_content_type' and '_content' parameters.
                method = getParameter("_method").toString(httpServletRequestMethod).toUpperCase();
            } else {
                method = httpServletRequestMethod;
            }
        }
        return method;
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
        Enumeration<String> names = getSession().getNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            values.put(name, getSession().get(name));
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

    /**
     * Get all query parameters
     */
    private Map<String, ParameterValue> getAllQueryParameters() {
        if (allQueryParameters == null) {
            Map<String, ParameterValue> tmp = new HashMap<>();
            tmp.putAll(parameters);
            allQueryParameters = Collections.unmodifiableMap(tmp);
            tmp = null;
        }

        return allQueryParameters;
    }

    /**
     * Get all path parameters
     */
    private Map<String, ParameterValue> getAllPathParameters() {
        if (allPathParameters == null) {
            Map<String, ParameterValue> tmp = new HashMap<>();
            if (pathParameters != null) {
                Set<String> names = pathParameters.keySet();
                for (String name : names) {
                    tmp.put(name, new ParameterValue(pathParameters.get(name)));
                }
            }
            allPathParameters = Collections.unmodifiableMap(tmp);
            tmp = null;
        }

        return allPathParameters;
    }

    public List<Cookie> getCookies() {
        return CookieUtils.getCookies(httpServletRequest);
    }

    public Cookie getCookie(String name) {
        return CookieUtils.getCookie(httpServletRequest, name);
    }

    public static Request get() {
        return RouteDispatcher.getRouteContext().getRequest();
    }

    @Override
    public String toString() {
        return "Request{" +
            "requestMethod='" + getMethod() + '\'' +
            ", uriPattern='" + getApplicationUri() + '\'' +
            '}';
    }

}
