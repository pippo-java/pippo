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
package ro.pippo.controller;

import ro.pippo.core.Application;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.route.DefaultRouteContext;
import ro.pippo.core.route.RouteMatch;
import ro.pippo.core.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author James Moger
 */
public class Context extends DefaultRouteContext {

    public Context(Application application, Request request, Response response, List<RouteMatch> routeMatches) {
        super(application, request, response, routeMatches);
    }

    public Context(Context context, List<RouteMatch> routeMatches) {
        this(context.getApplication(), context.getRequest(), context.getResponse(), routeMatches);
    }

    public Set<String> getAcceptTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.addAll(getContentTypes(getRequest().getAcceptType()));
        types.addAll(getContentTypes(getRequest().getHttpServletRequest().getHeader(HttpConstants.Header.ACCEPT)));

        return types;
    }

    public Set<String> getContentTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.addAll(getContentTypes(getRequest().getContentType()));
        types.addAll(getContentTypes(getRequest().getHttpServletRequest().getContentType()));

        return types;
    }

    /**
     * Cleans a complex content-type or accept header value by removing the
     * quality scores.
     * <p/>
     * <pre>
     * text/html,application/xhtml+xml,application/xml;q=0.9,image/webp
     * </pre>
     *
     * @param contentType
     * @return the sanitized set of content-types
     */
    protected Set<String> getContentTypes(String contentType) {
        if (StringUtils.isNullOrEmpty(contentType)) {
            return Collections.emptySet();
        }

        Set<String> set = new LinkedHashSet<>();
        String[] types = contentType.split(",");
        for (String type : types) {
            if (type.contains(";")) {
                // drop ;q=0.8 quality scores
                type = type.substring(0, type.indexOf(';'));
            }

            set.add(type.trim().toLowerCase());
        }

        return set;
    }

}
