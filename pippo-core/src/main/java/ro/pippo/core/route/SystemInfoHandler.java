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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Response;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Display information about system:
 * <ul>
 * <li>system properties</li>
 * <li>system environment</li>
 * </ul>
 * .
 * See {@link System#getenv()}.
 *
 * @author Decebal Suiu
 */
public class SystemInfoHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(SystemInfoHandler.class);

    @Override
    public void handle(RouteContext routeContext) {
        Response response = routeContext.getResponse().noCache().text();

        Map<String, String> props = new HashMap<>();
        for (String name : System.getProperties().stringPropertyNames()) {
            props.put(name, System.getProperty(name));
        }

        try (BufferedWriter writer = new BufferedWriter(response.getWriter())) {
            writeSystemProperties(props, writer);
            writer.newLine();

            writeEnv(System.getenv(), writer);

            writer.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void writeSystemProperties(Map<String, String> props, BufferedWriter writer) throws IOException {
        writeBanner("System properties", writer);
        writeProperties(props, writer);
    }

    protected void writeEnv(Map<String, String> env, BufferedWriter writer) throws IOException {
        writeBanner("Environment", writer);
        writeProperties(env, writer);
    }

    protected void writeBanner(String banner, BufferedWriter writer) throws IOException {
        writer.write("### " + banner + " ###");
        writer.newLine();
    }

    protected void writeProperties(Map<String, String> map, BufferedWriter writer) throws IOException {
        Set<String> keys = new TreeSet<>(map.keySet());
        for (String key : keys) {
            writer.write(key + " = " + map.get(key));
            writer.newLine();
        }
    }

}
