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
import java.util.*;

/**
 * Represents a server-side HTTP request. An instance of this class is created for each request.
 *
 * @author Decebal Suiu
 */
public class Request {

    private HttpServletRequest httpServletRequest;
    private Map<String, StringValue> parameters;

    private String body; // cache

    Request(HttpServletRequest servletRequest) {
        this.httpServletRequest = servletRequest;

        parameters = new HashMap<String, StringValue>();
        Enumeration<String> names = httpServletRequest.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            parameters.put(name, new StringValue(httpServletRequest.getParameter(name)));
        }
    }

    public Map<String, StringValue> getParameters() {
        return parameters;
    }

    public StringValue getParameter(String name) {
        if (!parameters.containsKey(name)) {
            return new StringValue(null);
        }

        return parameters.get(name);
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

    void addPathParameters(Map<String, String> pathParameters) {
        Set<String> names = pathParameters.keySet();
        for (String name : names) {
            parameters.put(name, new StringValue(pathParameters.get(name)));
        }
    }

}
