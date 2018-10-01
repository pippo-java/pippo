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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoRuntimeException;
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

    private static final Logger log = LoggerFactory.getLogger(CorsHandler.class);

    private String allowOrigin;

    private String exposeHeaders;

    private int maxAge = -1;

    private boolean allowCredentials;

    private String allowMethods;

    private String allowHeaders;

    public static Builder builder() {
        return new Builder();
    }

    private CorsHandler(Builder builder) {
        this.allowOrigin = join(builder.allowOrigins);
        if (this.allowOrigin == null) {
            throw new PippoRuntimeException("allowOrigin cannot be blank");
        }
        this.exposeHeaders = join(builder.exposeHeaders);
        this.maxAge = builder.maxAge;
        this.allowCredentials = builder.allowCredentials;
        this.allowMethods = join(builder.allowMethods);
        this.allowHeaders = join(builder.allowHeaders);
        log.info(String.format(
                "CorsHandler [allowOrigin=%s, allowMethods=%s, allowHeaders=%s, exposeHeaders=%s, maxAge=%s, allowCredentials=%s]",
                allowOrigin, allowMethods, allowHeaders, exposeHeaders, maxAge, allowCredentials));
    }

    @Override
    public void handle(RouteContext context) {
        context.getResponse().header(HttpConstants.Header.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin);

        if (exposeHeaders != null) {
            context.getResponse().header(HttpConstants.Header.ACCESS_CONTROL_EXPOSE_HEADERS, exposeHeaders);
        }

        if (maxAge != -1) {
            context.getResponse().header(HttpConstants.Header.ACCESS_CONTROL_MAX_AGE, "" + maxAge);
        }

        // According to the documentation only if true is what needs to be set
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Credentials#Directives
        if (allowCredentials) {
            context.getResponse().header(HttpConstants.Header.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        if (allowMethods != null) {
            context.getResponse().header(HttpConstants.Header.ACCESS_CONTROL_ALLOW_METHODS, allowMethods);
        }

        if (allowHeaders != null) {
            context.getResponse().header(HttpConstants.Header.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
        }

        if (context.getRequestMethod().equals("OPTIONS")) {
            context.getResponse().accepted();
            return;
        }

        context.next();
    }

    private String join(Set<String> values) {
        final String value = values.stream().filter(v -> !StringUtils.isNullOrEmpty(v))
                .collect(Collectors.joining(", "));
        return StringUtils.isNullOrEmpty(value) ? null : value;
    }

    public static class Builder {

        private Set<String> allowOrigins = new HashSet<>();
        private Set<String> exposeHeaders = new HashSet<>();
        private int maxAge = -1;
        private boolean allowCredentials;
        private Set<String> allowMethods = new HashSet<>();
        private Set<String> allowHeaders = new HashSet<>();

        private Builder() {
        }

        /**
         * Creates an instance of CorsHandler.
         *
         * @return instance of CorsHandler
         */
        public CorsHandler build() {
            return new CorsHandler(this);
        }

        /**
         * <b>Required!</b> The {@code Access-Control-Allow-Origin} response header
         * indicates whether the response can be shared with requesting code from the
         * given origin.
         *
         * @param origin
         *            origin, eg: http://pippo.ro
         *
         * @return this
         */
        public Builder addAllowOrigin(String origin) {
            this.allowOrigins.add(origin);
            return this;
        }

        /**
         * The {@code Access-Control-Expose-Headers} response header indicates which
         * headers can be exposed as part of the response by listing their names.
         *
         * @param header
         *            header name
         * @return this
         */
        public Builder addExposeHeader(String header) {
            this.exposeHeaders.add(header);
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
         *
         * @return this
         */
        public Builder setMaxAge(int maxAgeInSeconds) {
            this.maxAge = maxAgeInSeconds;
            return this;
        }

        /**
         * The Access-Control-Allow-Credentials response header indicates whether or not
         * the response to the request can be exposed to the page.
         *
         * @param allowCredentials
         *            true to expose, false otherwise
         *
         * @return this
         */
        public Builder allowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
            return this;
        }

        /**
         * The Access-Control-Allow-Methods response header specifies the method or
         * methods allowed when accessing the resource in response to a preflight
         * request.
         *
         * @param method
         *            http method
         *
         * @return this
         */
        public Builder addAllowMethod(String method) {
            this.allowMethods.add(method);
            return this;
        }

        /**
         * The {@code Access-Control-Allow-Headers} response header is used in response
         * to a preflight request which includes the
         * {@code Access-Control-Request-Headers} to indicate which HTTP headers can be
         * used during the actual request.
         *
         * @param header
         *            http header
         *
         * @return this
         */
        public Builder addAllowHeader(String header) {
            this.allowHeaders.add(header);
            return this;
        }

    }

}
