/*
 * Copyright (C) 2018 the original author or authors.
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
package ro.pippo.core.util;

import ro.pippo.core.compress.deflate.DeflaterResponseWrapper;
import ro.pippo.core.compress.gzip.GZipResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class HttpUtils {

    private HttpUtils() {}

    /**
     *
     * Returns {@link HttpServletResponseWrapper} based on request header `Accept-Encoding`
     * Here, gzip given higher priority than deflate
     * @param httpServletResponse - http response
     * @param httpServletRequest - http request
     * @return instance of response wrapper
     */
    public static HttpServletResponseWrapper getHttpResponseWrapper(
        HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest
    ) {
        String acceptEncoding = httpServletRequest.getHeader("accept-encoding");

        if (StringUtils.isNullOrEmpty(acceptEncoding)) {
            return new HttpServletResponseWrapper(httpServletResponse);
        } else if (acceptEncoding.contains("gzip") || acceptEncoding.contains("*")) {
            return new GZipResponseWrapper(httpServletResponse);
        } else if (acceptEncoding.contains("deflate")) {
            return new DeflaterResponseWrapper(httpServletResponse);
        } else {
            return new HttpServletResponseWrapper(httpServletResponse);
        }
    }
}
