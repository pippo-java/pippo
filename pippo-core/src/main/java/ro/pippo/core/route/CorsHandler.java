/*
 * Copyright (C) 2015 the original author or authors.
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
package ro.pippo.core.route;

import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.Response;
import ro.pippo.core.util.StringUtils;

/**
 * Define how CORS requests are handled.
 *
 * <p>
 * The Cross-Origin Resource Sharing standard works by adding new HTTP headers
 * that allow servers to describe the set of origins that are permitted to read
 * that information using a web browser.
 * </p>
 *
 * <p>
 * For more details see: https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
 * </p>
 */
public class CorsHandler implements RouteHandler<RouteContext> {

    private String allowOrigin;

    private String exposeHeaders;

    private int maxAge = -1;

    private boolean allowCredentials;

    private String allowMethods;

    private String allowHeaders;

    /**
     * The {@code Access-Control-Allow-Origin} response header indicates whether the
     * response can be shared with requesting code from the given origin.
     *
     * @param allowOrigin
     *            origin, eg: http://pippo.ro
     */
    public CorsHandler(String allowOrigin) {
        if (StringUtils.isNullOrEmpty(allowOrigin)) {
            throw new PippoRuntimeException("allowOrigin cannot be blank");
        }
        this.allowOrigin = allowOrigin;
    }

    @Override
    public void handle(RouteContext context) {
        final Response response = context.getResponse();

        response.header(HttpConstants.Header.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin);

        if (exposeHeaders != null) {
            response.header(HttpConstants.Header.ACCESS_CONTROL_EXPOSE_HEADERS, exposeHeaders);
        }

        if (maxAge != -1) {
            response.header(HttpConstants.Header.ACCESS_CONTROL_MAX_AGE, "" + maxAge);
        }

        // According to the documentation only if true is what needs to be set
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Credentials#Directives
        if (allowCredentials) {
            response.header(HttpConstants.Header.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        if (allowMethods != null) {
            response.header(HttpConstants.Header.ACCESS_CONTROL_ALLOW_METHODS, allowMethods);
        }

        if (allowHeaders != null) {
            response.header(HttpConstants.Header.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
        }

        if (context.getRequestMethod().equals("OPTIONS")) {
            response.accepted();
            return;
        }

        context.next();
    }

    /**
     * The {@code Access-Control-Expose-Headers} response header indicates which
     * headers can be exposed as part of the response by listing their names.
     *
     * @param exposeHeaders
     *            headers names, comma-separated
     */
    public CorsHandler exposeHeaders(String exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
        return this;
    }

    /**
     * The {@code Access-Control-Max-Age} response header indicates how long the
     * results of a preflight request (that is the information contained in the
     * {@code Access-Control-Allow-Methods} and {@code Access-Control-Allow-Headers}
     * headers) can be cached.
     *
     * @param maxAgeInSeconds
     *            max age in seconds
     */
    public CorsHandler maxAge(int maxAgeInSeconds) {
        this.maxAge = maxAgeInSeconds;
        return this;
    }

    /**
     * The Access-Control-Allow-Credentials response header indicates whether or not
     * the response to the request can be exposed to the page.
     *
     * @param allowCredentials
     *            true to expose, false otherwise
     */
    public CorsHandler allowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
        return this;
    }

    /**
     * The Access-Control-Allow-Methods response header specifies the method or
     * methods allowed when accessing the resource in response to a preflight
     * request.
     *
     *
     * @param allowMethods
     *            http methods names, comma-separated
     */
    public CorsHandler allowMethods(String allowMethods) {
        this.allowMethods = allowMethods;
        return this;
    }

    /**
     * The {@code Access-Control-Allow-Headers} response header is used in response
     * to a preflight request which includes the
     * {@code Access-Control-Request-Headers} to indicate which HTTP headers can be
     * used during the actual request.
     *
     * @param allowHeaders
     *            headers names, comma-separated
     */
    public CorsHandler allowHeaders(String allowHeaders) {
        this.allowHeaders = allowHeaders;
        return this;
    }

}