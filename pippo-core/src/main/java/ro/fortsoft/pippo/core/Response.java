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

    Response(HttpServletResponse httpServletResponse, Application application) {
        this.httpServletResponse = httpServletResponse;
        this.contentTypeEngines = application.getContentTypeEngines();
        this.templateEngine = application.getTemplateEngine();
        this.httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.toString());
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
     * A browser redirect.
     *
     * @param location Where to redirect
     */
    public void redirect(String location) {
        try {
            httpServletResponse.sendRedirect(location);
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
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
     * Send a not found (404 status code).
     *
     */
    public void notFound() {
        httpServletResponse.setStatus(HttpConstants.StatusCode.NOT_FOUND);
        try {
            httpServletResponse.sendError(HttpConstants.StatusCode.NOT_FOUND);
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
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

    public void text(Object object) {
        send(object, HttpConstants.ContentType.TEXT_PLAIN);
    }

    public void json(Object object) {
        send(object, HttpConstants.ContentType.APPLICATION_JSON);
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
