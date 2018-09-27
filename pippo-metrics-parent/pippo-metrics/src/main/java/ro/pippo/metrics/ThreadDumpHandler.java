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
package ro.pippo.metrics;

import com.codahale.metrics.jvm.ThreadDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Response;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

import java.lang.management.ManagementFactory;

/**
 * Returns a thread dump (thread states) as response.
 * If the thread dump is not supported then the status code returned is 500 (internal error).
 *
 * @author Decebal Suiu
 */
public class ThreadDumpHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(ThreadDumpHandler.class);

    private ThreadDump threadDump;

    public ThreadDumpHandler() {
        try {
            // some PaaS like Google App Engine blacklist "java.lang.management" package
            threadDump = new ThreadDump(ManagementFactory.getThreadMXBean());
        } catch (NoClassDefFoundError e) {
            log.warn("Thread dump isn't available", e);
        }
    }

    @Override
    public void handle(RouteContext routeContext) {
        Response response = routeContext.getResponse().noCache().text();

        if (threadDump != null) {
            threadDump.dump(response.getOutputStream());
        } else {
            response.internalError().send("Sorry your runtime environment does not allow to dump threads");
        }
    }

}
