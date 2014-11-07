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

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
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
                body = IOUtils.toString(httpServletRequest.getInputStream());
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
    void setPathParameters(Map<String, String> pathParameters) {
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
