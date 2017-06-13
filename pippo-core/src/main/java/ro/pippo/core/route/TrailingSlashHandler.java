/*
 * Copyright (C) 2017 the original author or authors.
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

import ro.pippo.core.Request;
import ro.pippo.core.util.StringUtils;

/**
 * Handler that add or remove the trailing slash.
 * Pippo treats a URI pattern with a trailing slash as different to one without.
 * That is, {@code /user} and {@code /user/} are different and so can have different callbacks attached.
 *
 * Using this handler you can have the same callback attached for both, the uri pattern with trailing slash
 * and the uri pattern without trailing slash.
 *
 * This handler uses redirect approach (add/remove trailing slash and redirect to new path).
 *
 * <p>
 * Note:
 * For GET requests a temporary redirect (302) is fine, but for other request methods like POST or PUT
 * the browser will send the second request with the GET method.
 * </p>
 *
 * @author Decebal Suiu
 */
public class TrailingSlashHandler implements RouteHandler {

    private boolean addSlash;

    public TrailingSlashHandler(boolean addSlash) {
        this.addSlash = addSlash;
    }

    @Override
    public void handle(RouteContext routeContext) {
        // get request path
        Request request = routeContext.getRequest();
        String path = request.getPath();

        // add or remove trailing slash
        if (path.length() > 1) {
            if (addSlash) {
                path = StringUtils.addEnd(path, "/");
            } else {
                path = StringUtils.removeEnd(path, "/");
            }
        }

        if (!path.equals(request.getPath())) {
            // redirect
            routeContext.redirect(path);
        } else {
            // continue with the next handler
            routeContext.next();
        }
    }

}
