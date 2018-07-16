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
package ro.pippo.core.route;

/**
 * Returns "pong" text as response.
 * The handler can be used to check the connection to the server.
 *
 * @author Decebal Suiu
 */
public class PingHandler implements RouteHandler {

    @Override
    public void handle(RouteContext routeContext) {
        routeContext.getResponse().noCache().text().send("pong");
    }

}
