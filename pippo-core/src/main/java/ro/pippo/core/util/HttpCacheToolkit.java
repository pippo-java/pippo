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
package ro.pippo.core.util;

import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.Request;
import ro.pippo.core.Response;

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpCacheToolkit adapted from Ninja Web Framework
 */
public class HttpCacheToolkit {

    private static final Logger log = LoggerFactory.getLogger(HttpCacheToolkit.class);

    private final PippoSettings pippoSettings;

    public HttpCacheToolkit(PippoSettings pippoSettings) {
        this.pippoSettings = pippoSettings;
    }

    public boolean isModified(String etag, long lastModified, Request request, Response response) {
        final String browserEtag = request.getHeader(HttpConstants.Header.IF_NONE_MATCH);
        if (browserEtag != null && !StringUtils.isNullOrEmpty(etag)) {
            return !(browserEtag.equals(etag));
        }

        final String ifModifiedSince = request.getHeader(HttpConstants.Header.IF_MODIFIED_SINCE);
        if ((lastModified > 0) && !StringUtils.isNullOrEmpty(ifModifiedSince)) {
            try {
                Date browserDate = DateUtils.parseHttpDateFormat(ifModifiedSince);
                if (browserDate.getTime() >= lastModified) {
                    return false;
                }
            } catch (ParseException e) {
                log.warn("Can't parse HTTP date", e);
            }
        }

        return true;
    }

    public void addEtag(Request request, Response response, long lastModified) {
        if (pippoSettings.isProd()) {
            String maxAge = pippoSettings.getString(PippoConstants.SETTING_HTTP_CACHE_CONTROL, "3600");
            if (maxAge.equals("0")) {
                response.header(HttpConstants.Header.CACHE_CONTROL, "no-cache");
            } else {
                response.header(HttpConstants.Header.CACHE_CONTROL, "max-age=" + maxAge);
            }
        } else {
            response.header(HttpConstants.Header.CACHE_CONTROL, "no-cache");
        }

        // Use etag on demand:
        String etag = null;

        boolean useEtag = pippoSettings.getBoolean(PippoConstants.SETTING_HTTP_USE_ETAG, true);
        if (useEtag) {
            // ETag right now is only lastModified long.
            // maybe we change that in the future.
            etag = "\"" + lastModified + "\"";
            response.header(HttpConstants.Header.ETAG, etag);
        }

        if (isModified(etag, lastModified, request, response)) {
            response.header(HttpConstants.Header.LAST_MODIFIED, DateUtils.formatForHttpHeader(lastModified));
        } else if (request.getMethod().equalsIgnoreCase(HttpConstants.Method.GET)) {
            response.status(HttpConstants.StatusCode.NOT_MODIFIED);
        }
    }

}
