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
import ro.pippo.core.gzip.GZipResponseWrapper;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteDispatcher;
import ro.pippo.core.util.DateUtils;
import ro.pippo.core.util.IoUtils;
import ro.pippo.core.util.MimeTypes;
import ro.pippo.core.util.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public final class Response {

    private static final Logger log = LoggerFactory.getLogger(Response.class);

    private HttpServletResponse httpServletResponse;
    private ContentTypeEngines contentTypeEngines;
    private TemplateEngine templateEngine;
    private Map<String, Object> locals;
    private Map<String, String> headers;
    private Map<String, Cookie> cookies;
    private String contextPath;
    private String applicationPath;
    private ResponseFinalizeListenerList finalizeListeners;
    private MimeTypes mimeTypes;

    private int status;
    private boolean chunked;

    public Response(HttpServletResponse httpServletResponse, Application application) {
        this.httpServletResponse = httpServletResponse;
        this.contentTypeEngines = application.getContentTypeEngines();
        this.templateEngine = application.getTemplateEngine();
        this.httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        this.contextPath = application.getRouter().getContextPath();
        this.applicationPath = StringUtils.removeEnd(application.getRouter().getApplicationPath(), "/");
        this.mimeTypes = application.getMimeTypes();

        this.status = 0;
    }

    /**
     * Map of bound objects which can be stored and shared between all handlers
     * for the current request/response cycle.
     * <p>
     * All bound objects are made available to the template engine during parsing.
     * </p>
     *
     * @return the bound objects map
     */
    public Map<String, Object> getLocals() {
        if (locals == null) {
            locals = new HashMap<>();
        }

        return locals;
    }

    /**
     * Shortcut for <code>getLocals().get("flash")</code>.
     * @return
     */
    public Flash getFlash() {
        return (Flash) getLocals().get("flash");
    }

    /**
     * Binds an object to the response.
     *
     * @param name
     * @param model
     * @return the response
     */
    public Response bind(String name, Object model) {
        getLocals().put(name, model);

        return this;
    }

    /**
     * Returns the servlet response.
     *
     * @return the servlet response
     */
    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    /**
     * Gets the character encoding of the response.
     *
     * @return the character encoding
     */
    public String getCharacterEncoding() {
        return getHttpServletResponse().getCharacterEncoding();
    }

    /**
     * Sets the character encoding of the response.
     *
     * @param charset
     * @return the response
     */
    public Response characterEncoding(String charset) {
        checkCommitted();

        getHttpServletResponse().setCharacterEncoding(charset);

        return this;
    }

    private void addCookie(Cookie cookie) {
        checkCommitted();
        if (StringUtils.isNullOrEmpty(cookie.getPath())) {
            cookie.setPath(StringUtils.addStart(contextPath, "/"));
        }
        getCookieMap().put(cookie.getName(), cookie);
    }

    /**
     * Adds a cookie to the response.
     *
     * @param cookie
     * @return the response
     */
    public Response cookie(Cookie cookie) {
        addCookie(cookie);

        return this;
    }

    /**
     * Adds a cookie to the response.
     *
     * @param name
     * @param value
     * @return the response
     */
    public Response cookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(true);
        addCookie(cookie);

        return this;
    }

    /**
     * Adds a cookie to the response.
     *
     * @param name
     * @param value
     * @param maxAge
     * @return the response
     */
    public Response cookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(true);
        cookie.setMaxAge(maxAge);
        addCookie(cookie);

        return this;
    }

    /**
     * Adds a cookie to the response.
     *
     * @param path
     * @param domain
     * @param name
     * @param value
     * @param maxAge
     * @param secure
     * @return the response
     */
    public Response cookie(String path, String domain, String name, String value, int maxAge, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setDomain(domain);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(secure);
        addCookie(cookie);

        return this;
    }

    /**
     * Returns all cookies added to the response.
     *
     * @return the cookies added to the response
     */
    public Collection<Cookie> getCookies() {
        return getCookieMap().values();
    }

    /**
     * Gets the specified cookie by name.
     *
     * @param name
     * @return the cookie or null
     */
    public Cookie getCookie(String name) {
        return getCookieMap().get(name);
    }

    /**
     * Removes the specified cookie by name.
     *
     * @param name
     * @return the response
     */
    public Response removeCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        addCookie(cookie);

        return this;
    }

    private Map<String, Cookie> getCookieMap() {
        if (cookies == null) {
            cookies = new HashMap<>();
        }

        return cookies;
    }

    private boolean isHeaderEmpty(String name) {
        return StringUtils.isNullOrEmpty(getHeaderMap().get(name));
    }

    /**
     * Sets a header.
     *
     * @param name
     * @param value
     * @return the response
     */
    public Response header(String name, String value) {
        checkCommitted();

        getHeaderMap().put(name, value);

        return this;
    }

    /**
     * Sets a header.
     *
     * @param name
     * @param value
     * @return the response
     */
    public Response header(String name, Date value) {
        checkCommitted();

        getHeaderMap().put(name, DateUtils.formatForHttpHeader(value));

        return this;
    }

    /**
     * Sets a filename header. It's a shortcut for {@code header(HttpConstants.Header.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")}.
     *
     * @param filename
     * @return
     */
    public Response filenameHeader(String filename) {
        if (filename != null && !filename.isEmpty()) {
            header(HttpConstants.Header.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        } else {
            header(HttpConstants.Header.CONTENT_DISPOSITION, "attachment; filename=\"\"");
        }

        return this;
    }

    /**
     * Returns a header value, if set in the Response.
     *
     * @param name
     * @return the header value or null
     */
    public String getHeader(String name) {
        return getHeaderMap().get(name);
    }

    private Map<String, String> getHeaderMap() {
        if (headers == null) {
            headers = new HashMap<>();
        }

        return headers;
    }

    /**
     * Sets this response as not cacheable.
     *
     * @return the response
     */
    public Response noCache() {
        checkCommitted();

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

    /**
     * Gets the status code of the response.
     *
     * @return the status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the status code of the response.
     *
     * @param status
     * @return the response
     */
    public Response status(int status) {
        checkCommitted();

        httpServletResponse.setStatus(status);
        this.status = status;

        return this;
    }

    /**
     * Sets the chunked transfer encoding.
     *
     * @return the response
     */
    public Response chunked() {
        return chunked(true);
    }

    /**
     * Controls the chunked transfer encoding.
     *
     * @param chunked
     * @return the response
     */
    public Response chunked(boolean chunked) {
        checkCommitted();

        this.chunked = chunked;

        return this;
    }

    /**
     * Redirect the browser to a location which may be...
     * <ul>
     * <li>relative to the current request
     * <li>relative to the servlet container root (if location starts with '/')
     * <li>an absolute url
     * </ul>
     * If you want a context-relative redirect, use the
     * {@link ro.pippo.core.Response#redirectToContextPath(String)} method.
     * If you want an application-relative redirect, use the
     * {@link ro.pippo.core.Response#redirectToApplicationPath(String)} method.
     * <p>This method commits the response.</p>
     *
     * @param location
     *            Where to redirect
     */
    public void redirect(String location) {
        checkCommitted();
        finalizeResponse();

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
     * <p>This method commits the response.</p>
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
     * Redirects the browser to a path relative to the Pippo application root. For
     * example, redirectToApplicationPath("/contacts") might redirect the browser to
     * http://localhost/myContext/myApp/contacts
     * <p>This method commits the response.</p>
     *
     * @param path
     */
    public void redirectToApplicationPath(String path) {
        if ("".equals(applicationPath)) {
            // application path is the root
            redirect(path);
        } else {
            redirect(applicationPath + StringUtils.addStart(path, "/"));
        }
    }

    /**
     * A permanent (3XX status code) redirect.
     * <p>This method commits the response.</p>
     *
     * @param location
     * @param statusCode
     */
    public void redirect(String location, int statusCode) {
        checkCommitted();
        finalizeResponse();

        status(statusCode);
        header(HttpConstants.Header.LOCATION, location);
        header(HttpConstants.Header.CONNECTION, "close");
        try {
            httpServletResponse.sendError(statusCode);
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

    /**
     * Set the response status to OK (200).
     * <p>
     * Standard response for successful HTTP requests. The actual response will
     * depend on the request method used. In a GET request, the response will
     * contain an entity corresponding to the requested resource. In a POST
     * request the response will contain an entity describing or containing the
     * result of the action.
     * </p>
     *
     */
    public Response ok() {
        status(HttpConstants.StatusCode.OK);

        return this;
    }

    /**
     * Set the response status to CREATED (201).
     * <p>
     * The request has been fulfilled and resulted in a new resource being created.
     * </p>
     *
     */
    public Response created() {
        status(HttpConstants.StatusCode.CREATED);

        return this;
    }

    /**
     * Set the response status to ACCEPTED (202).
     * <p>
     * The request has been accepted for processing, but the processing has not
     * been completed. The request might or might not eventually be acted upon,
     * as it might be disallowed when processing actually takes place.
     * </p>
     *
     */
    public Response accepted() {
        status(HttpConstants.StatusCode.ACCEPTED);

        return this;
    }

    /**
     * Set the response status to BAD REQUEST (400).
     * <p>
     * The server cannot or will not process the request due to something that
     * is perceived to be a client error.
     * </p>
     *
     */
    public Response badRequest() {
        status(HttpConstants.StatusCode.BAD_REQUEST);

        return this;
    }

    /**
     * Set the response status to UNAUTHORIZED (401).
     * <p>
     * Similar to 403 Forbidden, but specifically for use when authentication is
     * required and has failed or has not yet been provided. The response must
     * include a WWW-Authenticate header field containing a challenge applicable
     * to the requested resource.
     * </p>
     */
    public Response unauthorized() {
        status(HttpConstants.StatusCode.UNAUTHORIZED);

        return this;
    }

    /**
     * Set the response status to PAYMENT REQUIRED (402).
     * <p>
     * Reserved for future use. The original intention was that this code might
     * be used as part of some form of digital cash or micropayment scheme, but
     * that has not happened, and this code is not usually used.
     * </p>
     */
    public Response paymentRequired() {
        status(HttpConstants.StatusCode.PAYMENT_REQUIRED);

        return this;
    }

    /**
     * Set the response status to FORBIDDEN (403).
     * <p>
     * The request was a valid request, but the server is refusing to respond to
     * it. Unlike a 401 Unauthorized response, authenticating will make no
     * difference.
     * </p>
     *
     */
    public Response forbidden() {
        status(HttpConstants.StatusCode.FORBIDDEN);

        return this;
    }

    /**
     * Set the response status to NOT FOUND (404).
     * <p>
     * The requested resource could not be found but may be available again in
     * the future. Subsequent requests by the client are permissible.
     * </p>
     *
     */
    public Response notFound() {
        status(HttpConstants.StatusCode.NOT_FOUND);

        return this;
    }

    /**
     * Set the response status to METHOD NOT ALLOWED (405).
     * <p>
     * A request was made of a resource using a request method not supported
     * by that resource; for example, using GET on a form which requires data
     * to be presented via POST, or using PUT on a read-only resource.
     * </p>
     *
     */
    public Response methodNotAllowed() {
        status(HttpConstants.StatusCode.METHOD_NOT_ALLOWED);

        return this;
    }

    /**
     * Set the response status to CONFLICT (409).
     * <p>
     * Indicates that the request could not be processed because of conflict in
     * the request, such as an edit conflict in the case of multiple updates.
     * </p>
     *
     */
    public Response conflict() {
        status(HttpConstants.StatusCode.CONFLICT);

        return this;
    }

    /**
     * Set the response status to GONE (410).
     * <p>
     * Indicates that the resource requested is no longer available and will not
     * be available again. This should be used when a resource has been
     * intentionally removed and the resource should be purged. Upon receiving a
     * 410 status code, the client should not request the resource again in the
     * future.
     * </p>
     *
     */
    public Response gone() {
        status(HttpConstants.StatusCode.GONE);

        return this;
    }

    /**
     * Set the response status to INTERNAL ERROR (500).
     * <p>
     * A generic error message, given when an unexpected condition was
     * encountered and no more specific message is suitable.
     * </p>
     *
     */
    public Response internalError() {
        status(HttpConstants.StatusCode.INTERNAL_ERROR);

        return this;
    }

    /**
     * Set the response status to NOT IMPLEMENTED (501).
     * <p>
     * The server either does not recognize the request method, or it lacks the
     * ability to fulfil the request. Usually this implies future availability
     * (e.g., a new feature of a web-service API).
     * </p>
     *
     */
    public Response notImplemented() {
        status(HttpConstants.StatusCode.NOT_IMPLEMENTED);

        return this;
    }

    /**
     * Set the response status to OVERLOADED (502).
     * <p>
     * The server is currently unavailable (because it is overloaded or down
     * for maintenance). Generally, this is a temporary state.
     * </p>
     */
    public Response overloaded() {
        status(HttpConstants.StatusCode.OVERLOADED);

        return this;
    }

    /**
     * Set the response status to SERVICE UNAVAILABLE (503).
     * <p>
     * The server is currently unavailable (because it is overloaded or down
     * for maintenance). Generally, this is a temporary state.
     * </p>
     */
    public Response serviceUnavailable() {
        status(HttpConstants.StatusCode.SERVICE_UNAVAILABLE);

        return this;
    }

    /**
     * Sets the content length of the response.
     *
     * @param length
     * @return the response
     */
    public Response contentLength(long length) {
        checkCommitted();

        httpServletResponse.setContentLength((int) length);

        return this;
    }

    /**
     * Returns the content type of the response.
     *
     * @return the content type
     */
    public String getContentType() {
        String contentType = getHeader(HttpConstants.Header.CONTENT_TYPE);
        if (contentType == null) {
            contentType = httpServletResponse.getContentType();
        }

        return contentType;
    }

    /**
     * Sets the content type of the response.
     *
     * @param contentType
     * @return the response
     */
    public Response contentType(String contentType) {
        checkCommitted();

        header(HttpConstants.Header.CONTENT_TYPE, contentType);
        httpServletResponse.setContentType(contentType);

        return this;
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
        if ("*/*".equals(request.getAcceptType())) {
            // client accepts all types
            return this;
        }

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

    /**
     * Sets the Response content-type to application/xml.
     */
    public Response xml() {
        return contentType(HttpConstants.ContentType.APPLICATION_XML);
    }

    /**
     * Sets the Response content-type to application/x-yaml.
     */
    public Response yaml() {
        return contentType(HttpConstants.ContentType.APPLICATION_X_YAML);
    }

    /**
     * Writes the string content directly to the response.
     * <p>This method commits the response.</p>
     *
     * @param content
     */
    public void send(CharSequence content) {
        checkCommitted();

        commit(content);
    }

    /**
     * Replaces '{}' in the format string with the supplied arguments and
     * writes the string content directly to the response.
     * <p>This method commits the response.</p>
     *
     * @param format
     * @param args
     */
    public void send(String format, Object... args) {
        checkCommitted();

        commit(StringUtils.format(format, args));
    }

    /**
     * Serializes the object as JSON using the registered <code>application/json</code>
     * ContentTypeEngine and writes it to the response.
     * <p>This method commits the response.</p>
     *
     * @param object
     */
    public void json(Object object) {
        send(object, HttpConstants.ContentType.APPLICATION_JSON);
    }

    /**
     * Serializes the object as XML using the registered <code>application/xml</code>
     * ContentTypeEngine and writes it to the response.
     * <p>This method commits the response.</p>
     *
     * @param object
     */
    public void xml(Object object) {
        send(object, HttpConstants.ContentType.APPLICATION_XML);
    }

    /**
     * Serializes the object as YAML using the registered <code>application/x-yaml</code>
     * ContentTypeEngine and writes it to the response.
     * <p>This method commits the response.</p>
     *
     * @param object
     */
    public void yaml(Object object) {
        send(object, HttpConstants.ContentType.APPLICATION_X_YAML);
    }

    /**
     * Serializes the object as plain text using the registered <code>text/plain</code>
     * ContentTypeEngine and writes it to the response.
     * <p>This method commits the response.</p>
     *
     * @param object
     */
    public void text(Object object) {
        send(object, HttpConstants.ContentType.TEXT_PLAIN);
    }

    /**
     * Serializes the object using the registered ContentTypeEngine matching
     * the pre-specified content-type.
     * <p>This method commits the response.</p>
     *
     * @param object
     */
    public void send(Object object) {
        if (object instanceof File) {
            file((File) object);
        } else {
            send(object, getContentType());
        }
    }

    private void send(Object object, String contentType) {
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

    /**
     * Copies the input stream to the response output stream and closes the input stream upon completion.
     * <p>This method commits the response.</p>
     *
     * @param input
     */
    public void resource(InputStream input) {
        checkCommitted();
        finalizeResponse();

        // content type to OCTET_STREAM if it's not set
        if (getContentType() == null) {
            contentType(HttpConstants.ContentType.APPLICATION_OCTET_STREAM);
        }

        try {
            send(input);
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

    /**
     * Writes the specified file directly to the response.
     * <p>This method commits the response.</p>
     *
     * @param file
     */
    public void file(File file) {
        try {
            contentLength(file.length());
            file(file.getName(), new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new PippoRuntimeException(e);
        }
    }

    /**
     * Copies the input stream to the response output stream as a download and closes the input stream upon completion.
     * <p>This method commits the response.</p>
     *
     * @param filename
     * @param input
     */
    public void file(String filename, InputStream input) {
        checkCommitted();

        // content type to OCTET_STREAM if it's not set
        if (getContentType() == null) {
            contentType(mimeTypes.getContentType(filename, HttpConstants.ContentType.APPLICATION_OCTET_STREAM));
        }

        if (isHeaderEmpty(HttpConstants.Header.CONTENT_DISPOSITION)) {
            filenameHeader(filename);
        }

        finalizeResponse();

        try {
            send(input);
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

    private void send(InputStream input) throws IOException {
        try {
            // by calling httpServletResponse.getOutputStream() we are committing the response
            IoUtils.copy(input, httpServletResponse.getOutputStream());

            if (chunked) {
                // flushing the buffer forces chunked-encoding
                httpServletResponse.flushBuffer();
            }
        } finally {
            IoUtils.close(input);
        }
    }

    /**
     * Renders a template and writes the output directly to the response.
     * <p>This method commits the response.</p>
     *
     * @param templateName
     */
    public void render(String templateName) {
        render(templateName, new HashMap<>());
    }

    /**
     * Renders a template and writes the output directly to the response.
     * <p>This method commits the response.</p>
     *
     * @param templateName
     * @param model
     */
    public void render(String templateName, Map<String, Object> model) {
        send(renderToString(templateName, model));
    }

    /**
     * Renders a template to a {@link String}.
     *
     * @param templateName
     */
    public String renderToString(String templateName) {
        return renderToString(templateName, new HashMap<>());
    }

    /**
     * Renders a template to a {@link String}.
     *
     * @param templateName
     * @param model
     */
    public String renderToString(String templateName, Map<String, Object> model) {
        if (templateEngine == null) {
            throw new PippoRuntimeException("You must set a template engine in your application");
        }

        // merge the model passed with the locals data
        model.putAll(getLocals());

        // add session (if exists) to model
        Session session = Session.get();
        if (session != null) {
            model.put("session", session);
        }

        // render the template using the merged model
        StringWriter stringWriter = new StringWriter();
        templateEngine.renderResource(templateName, model, stringWriter);

        return stringWriter.toString();
    }

    private void checkCommitted() {
        if (isCommitted()) {
            throw new PippoRuntimeException("The response has already been committed");
        }
    }

    /**
     * Returns true if this response has already been committed.
     *
     * @return true if the response has been committed
     */
    public boolean isCommitted() {
        return httpServletResponse.isCommitted();
    }

    /**
     * This method commits the response.
     */
    public void commit() {
        commit(null);
    }

    private void commit(CharSequence content) {
        checkCommitted();
        finalizeResponse();

        // content type to TEXT_HTML if it's not set
        if (getContentType() == null) {
            contentType(HttpConstants.ContentType.TEXT_HTML);
        }

        try {
            if (content != null) {
                contentLength(content.toString().getBytes().length);
                httpServletResponse.getWriter().append(content);
            }
            log.trace("Response committed");
            if (chunked) {
                httpServletResponse.flushBuffer();
            }

            finishGZip();
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

    private void finalizeResponse() {
        // add headers
        for (Map.Entry<String, String> header : getHeaderMap().entrySet()) {
            httpServletResponse.setHeader(header.getKey(), header.getValue());
        }

        // add cookies
        for (Cookie cookie : getCookies()) {
            httpServletResponse.addCookie(cookie);
        }

        // set status to OK if it's not set
        if (getStatus() == 0 || getStatus() == Integer.MAX_VALUE) {
            ok();
        }

        // call finalize listeners
        if ((finalizeListeners != null) && !finalizeListeners.isEmpty()) {
            finalizeListeners.onFinalize(this);
        }
    }

    /*
     * Finish the GZip response.
     */
    private void finishGZip() {
        if (httpServletResponse instanceof GZipResponseWrapper) {
            GZipResponseWrapper responseWrapper = (GZipResponseWrapper) httpServletResponse;
            responseWrapper.finish();
        }
    }

    public ResponseFinalizeListenerList getFinalizeListeners() {
        if (finalizeListeners == null) {
            finalizeListeners = new ResponseFinalizeListenerList();
        }

        return finalizeListeners;
    }

    public ServletOutputStream getOutputStream() {
        checkCommitted();
        finalizeResponse();

        // content type to OCTET_STREAM if it's not set
        if (getContentType() == null) {
            contentType(HttpConstants.ContentType.APPLICATION_OCTET_STREAM);
        }

        try {
            return httpServletResponse.getOutputStream();
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

    public PrintWriter getWriter() {
        checkCommitted();
        finalizeResponse();

        if (getContentType() == null) {
            contentType(HttpConstants.ContentType.TEXT_HTML);
        }

        try {
            return httpServletResponse.getWriter();
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

    /**
     * This method resets the response.
     * Clears any data that exists in the buffer as well as the status code, cookies, locals and headers.
     * In the end, {@link HttpServletResponse#reset()} is called.
     */
    public void reset() {
        checkCommitted();

        // reset all headers
        headers = new HashMap<>();
        // reset all cookies
        cookies = new HashMap<>();
        // reset all locales
        locals = new HashMap<>();
        // set status to 0 or INT MAX
        status = 0;

        try {
            httpServletResponse.reset();
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    public static Response get() {
        RouteContext routeContext = RouteDispatcher.getRouteContext();

        return (routeContext != null) ? routeContext.getResponse() : null;
    }

}
