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
package ro.fortsoft.pippo.core;

import ro.fortsoft.pippo.core.util.IoUtils;
import ro.fortsoft.pippo.core.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Decebal Suiu
 */
public class Response {

    private static final Logger log = LoggerFactory.getLogger(Response.class);

    private HttpServletResponse httpServletResponse;
    private ContentTypeEngines contentTypeEngines;
    private TemplateEngine templateEngine;
    private Map<String, Object> locals;
    private Map<String, Cookie> cookies;
    private String contextPath;

    Response(HttpServletResponse httpServletResponse, Application application) {
        this.httpServletResponse = httpServletResponse;
        this.contentTypeEngines = application.getContentTypeEngines();
        this.templateEngine = application.getTemplateEngine();
        this.httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        this.contextPath = application.getRouter().getContextPath();
    }

    public String getContentType() {
        return httpServletResponse.getContentType();
    }

    public Response contentType(String contentType) {
        httpServletResponse.setContentType(contentType);

        return this;
    }

    public Response characterEncoding(String charset) {
        getHttpServletResponse().setCharacterEncoding(charset);

        return this;
    }

    public String getCharacterEncoding() {
        return getHttpServletResponse().getCharacterEncoding();
    }

    public Response contentLength(long length) {
        httpServletResponse.addHeader(HttpConstants.Header.CONTENT_LENGTH, Long.toString(length));

        return this;
    }

    public Response header(String name, String value) {
        httpServletResponse.addHeader(name, value);

        return this;
    }

    /**
     * Redirect the browser to a location which may be...
     * <ul>
     * <li>relative to the current request
     * <li>relative to the servlet container root (if location starts with '/')
     * <li>an absolute url
     * </ul>
     * If you want a context-relative redirect, use the {@link redirectToContextPath}
     * method.
     *
     * @param location
     *            Where to redirect
     */
    public void redirect(String location) {
        try {
            httpServletResponse.sendRedirect(location);
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

    /**
     * Redirects the browser to a path relative to the application context. For
     * example, redirectToContextPath("/contacts") might redirect the browser to
     * http://localhost/myContext/contacts
     *
     * @param path
     */
    public void redirectToContextPath(String path) {
        if ("".equals(contextPath)) {
            // context path is the root
            redirect(path);
        } else {
            redirect(contextPath + StringUtils.addStart(path, "/"));
        }
    }

    /**
     * A permanent (3XX status code) redirect.
     */
    public void redirect(String location, int statusCode) {
        httpServletResponse.setStatus(statusCode);
        httpServletResponse.setHeader(HttpConstants.Header.LOCATION, location);
        httpServletResponse.setHeader(HttpConstants.Header.CONNECTION, "close");
        try {
            httpServletResponse.sendError(statusCode);
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

    /**
     * Send an OK (200 status code).
     * <p>
     * Standard response for successful HTTP requests. The actual response will
     * depend on the request method used. In a GET request, the response will
     * contain an entity corresponding to the requested resource. In a POST
     * request the response will contain an entity describing or containing the
     * result of the action.
     * </p>
     *
     */
    public void sendOk() {
        status(HttpConstants.StatusCode.OK);
        commit();
    }

    /**
     * Send an CREATED (201 status code).
     * <p>
     * The request has been fulfilled and resulted in a new resource being created.
     * </p>
     *
     */
    public void sendCreated() {
        status(HttpConstants.StatusCode.CREATED);
        commit();
    }

    /**
     * Send an ACCEPTED (202 status code).
     * <p>
     * The request has been accepted for processing, but the processing has not
     * been completed. The request might or might not eventually be acted upon,
     * as it might be disallowed when processing actually takes place.
     * </p>
     *
     */
    public void sendAccepted() {
        status(HttpConstants.StatusCode.ACCEPTED);
        commit();
    }

    /**
     * Send an error.
     *
     */
    public void sendError(int statusCode) {
        httpServletResponse.setStatus(statusCode);
        try {
            httpServletResponse.sendError(statusCode);
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

    /**
     * Send a BAD REQUEST (400 status code).
     * <p>
     * The server cannot or will not process the request due to something that
     * is perceived to be a client error.
     * </p>
     *
     */
    public void sendBadRequest() {
        sendError(HttpConstants.StatusCode.BAD_REQUEST);
    }

    /**
     * Send UNAUTHORIZED (401 status code).
     * <p>
     * Similar to 403 Forbidden, but specifically for use when authentication is
     * required and has failed or has not yet been provided. The response must
     * include a WWW-Authenticate header field containing a challenge applicable
     * to the requested resource.
     * </p>
     */
    public void sendUnauthorized() {
        sendError(HttpConstants.StatusCode.UNAUTHORIZED);
    }

    /**
     * Send FORBIDDEN (403 status code).
     *
     * <p>
     * The request was a valid request, but the server is refusing to respond to
     * it. Unlike a 401 Unauthorized response, authenticating will make no
     * difference.
     * </p>
     *
     */
    public void sendForbidden() {
        sendError(HttpConstants.StatusCode.FORBIDDEN);
    }

    /**
     * Send a NOT FOUND (404 status code).
     * <p>
     * The requested resource could not be found but may be available again in
     * the future. Subsequent requests by the client are permissible.
     * </p>
     *
     */
    public void sendNotFound() {
        sendError(HttpConstants.StatusCode.NOT_FOUND);
    }

    /**
     * Send a CONFLICT (409 status code).
     * <p>
     * Indicates that the request could not be processed because of conflict in
     * the request, such as an edit conflict in the case of multiple updates.
     * </p>
     *
     */
    public void sendConflict() {
        sendError(HttpConstants.StatusCode.CONFLICT);
    }

    /**
     * Send GONE (410 status code).
     * <p>
     * Indicates that the resource requested is no longer available and will not
     * be available again. This should be used when a resource has been
     * intentionally removed and the resource should be purged. Upon receiving a
     * 410 status code, the client should not request the resource again in the
     * future.
     * </p>
     *
     */
    public void sendGone() {
        sendError(HttpConstants.StatusCode.GONE);
    }

    /**
     * Send an INTERNAL ERROR (500 status code).
     * <p>
     * A generic error message, given when an unexpected condition was
     * encountered and no more specific message is suitable.
     * </p>
     *
     */
    public void sendInternalError() {
        sendError(HttpConstants.StatusCode.INTERNAL_ERROR);
    }

    /**
     * Send an NOT IMPLEMENTED ERROR (501 status code).
     * <p>
     * The server either does not recognize the request method, or it lacks the
     * ability to fulfil the request. Usually this implies future availability
     * (e.g., a new feature of a web-service API).
     * </p>
     *
     */
    public void sendNotImplemented() {
        sendError(HttpConstants.StatusCode.NOT_IMPLEMENTED);
    }

    public Response cookie(Cookie cookie) {
        addCookie(cookie);

        return this;
    }

    public Response cookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        addCookie(cookie);

        return this;
    }

    public Response cookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        addCookie(cookie);

        return this;
    }

    public Response cookie(String path, String domain, String name, String value, int maxAge, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setDomain(domain);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(secure);
        addCookie(cookie);

        return this;
    }

    public Collection<Cookie> getCookies() {
        return getCookieMap().values();
    }

    public Cookie getCookie(String name) {
        return getCookieMap().get(name);
    }

    public Response removeCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        addCookie(cookie);

        return this;
    }

    public int getStatus() {
        return httpServletResponse.getStatus();
    }

    public Response status(int status) {
        httpServletResponse.setStatus(status);

        return this;
    }

    public Response noCache() {
        // no-cache headers for HTTP/1.1
        header(HttpConstants.Header.CACHE_CONTROL, "no-store, no-cache, must-revalidate");

        // no-cache headers for HTTP/1.1 (IE)
        header(HttpConstants.Header.CACHE_CONTROL, "post-check=0, pre-check=0");

        // no-cache headers for HTTP/1.0
        header(HttpConstants.Header.PRAGMA, "no-cache");

        // set the expires to past
        httpServletResponse.setDateHeader("Expires", 0);

        return this;
    }

    public void write(CharSequence sequence) {
        try {
            httpServletResponse.getWriter().append(sequence);
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    public boolean isCommitted() {
        return httpServletResponse.isCommitted();
    }

    /**
     * Attempts to set the Content-Type of the Response based on Request
     * headers.
     * <p>
     * The Accept header is preferred for negotiation but the Content-Type
     * header may also be used if an agreeable engine can not be determined.
     * </p>
     * <p>
     * If no Content-Type can not be negotiated then the response will not be
     * modified. This behavior allows specification of a default Content-Type
     * using one of the methods such as <code>xml()</code> or <code>json()</code>.
     * <p>
     * For example, <code>response.xml().contentType(request).send(myObject);</code>
     * would set the default Content-Type as <code>application/xml</code> and
     * then attempt to negotiate the client's preferred type. If negotiation failed,
     * then the default <code>application/xml</code> would be sent and used to
     * serialize the outgoing object.
     *
     * @param request
     * @return the response
     */
    public Response contentType(Request request) {
        // prefer the Accept header
        ContentTypeEngine engine = contentTypeEngines.getContentTypeEngine(request.getAcceptType());
        if (engine != null) {
            log.debug("Negotiated '{}' from request Accept header", engine.getContentType());
        } else if (!StringUtils.isNullOrEmpty(request.getContentType())) {
            // try to match the Request content-type
            engine = contentTypeEngines.getContentTypeEngine(request.getContentType());
            if (engine != null) {
                log.debug("Negotiated '{}' from request Content-Type header", engine.getContentType());
            }
        }

        if (engine == null) {
            log.debug("Failed to negotiate a content type for Accept='{}' and Content-Type='{}'",
                    request.getAcceptType(), request.getContentType());
            return this;
        }
        return contentType(engine.getContentType());
    }

    /**
     * Sets the Response content-type to text/plain.
     */
    public Response text() {
        return contentType(HttpConstants.ContentType.TEXT_PLAIN);
    }

    public void text(Object object) {
        send(object, HttpConstants.ContentType.TEXT_PLAIN);
    }

    /**
     * Sets the Response content-type to text/html.
     */
    public Response html() {
        return contentType(HttpConstants.ContentType.TEXT_HTML);
    }

    /**
     * Sets the Response content-type to application/json.
     */
    public Response json() {
        return contentType(HttpConstants.ContentType.APPLICATION_JSON);
    }

    public void json(Object object) {
        send(object, HttpConstants.ContentType.APPLICATION_JSON);
    }

    /**
     * Sets the Response content-type to application/xml.
     */
    public Response xml() {
        return contentType(HttpConstants.ContentType.APPLICATION_XML);
    }

    public void xml(Object object) {
        send(object, HttpConstants.ContentType.APPLICATION_XML);
    }

    public void send(Object object) {
        send(object, getContentType());
    }

    public void send(Object object, String contentType) {
        if (StringUtils.isNullOrEmpty(contentType)) {
            throw new PippoRuntimeException("You must specify a content type!");
        }
        ContentTypeEngine contentTypeEngine = contentTypeEngines.getContentTypeEngine(contentType);
        if (contentTypeEngine == null) {
            throw new PippoRuntimeException("You must set a content type engine for '{}'", contentType);
        }

        header(HttpConstants.Header.CONTENT_TYPE, contentTypeEngine.getContentType());
        send(contentTypeEngine.toString(object));
    }

    public void send(CharSequence content) {
        checkCommitted();
        write(content);
        commit();
    }

    public void resource(InputStream input) {
        checkCommitted();

        if (isHeaderEmpty(HttpConstants.Header.CONTENT_TYPE)) {
            header(HttpConstants.Header.CONTENT_TYPE, HttpConstants.ContentType.APPLICATION_OCTET_STREAM);
        }

        try {
            long length = IoUtils.copy(input, httpServletResponse.getOutputStream());
            if (isHeaderEmpty(HttpConstants.Header.CONTENT_LENGTH)) {
                contentLength(length);
            }

            commit();
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        } finally {
            IoUtils.close(input);
        }
    }

    public void file(File file) {
        try {
            file(file.getName(), new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new PippoRuntimeException(e);
        }
    }

    public void file(String filename, InputStream input) {
        checkCommitted();

//        if (isHeaderEmpty(HttpConstants.Header.CONTENT_TYPE)) {
            header(HttpConstants.Header.CONTENT_TYPE, HttpConstants.ContentType.APPLICATION_OCTET_STREAM);
//        }

        if (isHeaderEmpty(HttpConstants.Header.CONTENT_DISPOSITION)) {
            if (filename != null && !filename.isEmpty()) {
//                header(HttpConstants.Header.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
                header(HttpConstants.Header.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            } else {
                header(HttpConstants.Header.CONTENT_DISPOSITION, "attachment; filename=\"\"");
            }
        }

        try {
            long length = IoUtils.copy(input, httpServletResponse.getOutputStream());
            if (isHeaderEmpty(HttpConstants.Header.CONTENT_LENGTH)) {
                contentLength(length);
            }

            commit();
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        } finally {
            IoUtils.close(input);
        }
    }

    public Response bind(String name, Object model) {
        getLocals().put(name, model);

        return this;
    }

    public void render(String templateName) {
        render(templateName, new HashMap<String, Object>());
    }

    public void render(String templateName, Map<String, Object> model) {
        if (templateEngine == null) {
            log.error("You must set a template engine first");
            return;
        }

        // merge the model passed with the locals data
        model.putAll(getLocals());

        // render the template using the merged model
        StringWriter stringWriter = new StringWriter();
        templateEngine.render(templateName, model, stringWriter);
        send(stringWriter.toString());
    }

    /**
     * Good for storing variables for the current request/response cycle.
     * Also these variables will be available automatically to all templates for the current request/response cycle.
     *
     * @return
     */
    public Map<String, Object> getLocals() {
        if (locals == null) {
            locals = new HashMap<>();
        }

        return locals;
    }

    private boolean isHeaderEmpty(String name) {
        String value = getHttpServletResponse().getHeader(name);
        return (value == null) || value.isEmpty();
    }

    private void checkCommitted() {
        if (isCommitted()) {
            throw new PippoRuntimeException("The response has already been committed");
        }
    }

    private Map<String, Cookie> getCookieMap() {
        if (cookies == null) {
            cookies = new HashMap<>();
        }

        return cookies;
    }

    private void addCookie(Cookie cookie) {
        getCookieMap().put(cookie.getName(), cookie);
    }

    public void commit() {
        // add cookies
        for (Cookie cookie : getCookies()) {
            httpServletResponse.addCookie(cookie);
        }

        // set status to OK if it's not set
        if (getStatus() == 0) {
            status(HttpConstants.StatusCode.OK);
        }

        // content type to TEXT_HTML if it's not set
        if (getContentType() == null) {
            header(HttpConstants.Header.CONTENT_TYPE, HttpConstants.ContentType.TEXT_HTML);
        }

        try {
            httpServletResponse.flushBuffer();
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

}
