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

/**
 * @author Decebal Suiu
 */
public final class HttpConstants {

    public static final class Method {

        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String DELETE = "DELETE";
        public static final String HEAD = "HEAD";
        public static final String PUT = "PUT";
        public static final String PATCH = "PATCH";
        public static final String CONNECT = "CONNECT";
        public static final String OPTIONS = "OPTIONS";

        // not really a method (marker for any methods)
        public static final String ANY = "ANY";

        /**
         * @deprecated Replaced by {@link #ANY}.
         */
        @Deprecated
        public static final String ALL = ANY;

        private Method() {
            // restrict instantiation
        }

    }

    public static final class StatusCode {

        public static final int OK = 200;
        public static final int CREATED = 201;
        public static final int ACCEPTED = 202;
        public static final int PARTIAL_INFO = 203;
        public static final int NO_RESPONSE = 204;
        public static final int MOVED = 301;
        public static final int FOUND = 302;
        public static final int METHOD = 303;
        public static final int NOT_MODIFIED = 304;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int PAYMENT_REQUIRED = 402;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int METHOD_NOT_ALLOWED = 405;
        public static final int CONFLICT = 409;
        public static final int GONE = 410;
        public static final int INTERNAL_ERROR = 500;
        public static final int NOT_IMPLEMENTED = 501;
        public static final int OVERLOADED = 502;
        public static final int SERVICE_UNAVAILABLE = 503;
        public static final int GATEWAY_TIMEOUT = 504;

        private StatusCode() {
            // restrict instantiation
        }

    }

    public static final class Header {

        public static final String ACCEPT = "Accept";
        public static final String ACCEPT_CHARSET = "Accept-Charset";
        public static final String ACCEPT_ENCODING = "Accept-Encoding";
        public static final String ACCEPT_LANGUAGE = "Accept-Language";
        public static final String ACCEPT_DATETIME = "Accept-Datetime";
        public static final String AUTHORIZATION = "Authorization";
        public static final String PRAGMA = "Pragma";
        public static final String CACHE_CONTROL = "Cache-Control";
        public static final String CONNECTION = "Connection";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONTENT_LENGTH = "Content-Length";
        public static final String CONTENT_MD5 = "Content-MD5";
        public static final String CONTENT_DISPOSITION = "Content-Disposition";
        public static final String DATE = "Date";
        public static final String ETAG = "Etag";
        public static final String IF_MATCH = "If-Match";
        public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
        public static final String IF_NONE_MATCH = "If-None-Match";
        public static final String USER_AGENT = "User-Agent";
        public static final String HOST = "Host";
        public static final String LAST_MODIFIED = "Last-Modified";
        public static final String LOCATION = "Location";
        public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
        public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
        public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
        public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
        public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
        public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

        private Header() {
            // restrict instantiation
        }

    }

    public static final class ContentType {

        public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
        public static final String APPLICATION_JSON = "application/json";
        public static final String APPLICATION_XML = "application/xml";
        public static final String APPLICATION_X_YAML = "application/x-yaml";
        public static final String TEXT_HTML = "text/html";
        public static final String TEXT_XHTML = "text/xhtml";
        public static final String TEXT_PLAIN = "text/plain";
        public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
        public static final String MULTIPART_FORM_DATA = "multipart/form-data";

        private ContentType() {
            // restrict instantiation
        }

    }

    private HttpConstants() {
        // restrict instantiation
    }

}
