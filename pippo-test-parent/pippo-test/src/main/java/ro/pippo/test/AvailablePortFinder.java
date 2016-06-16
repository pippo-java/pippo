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
package ro.pippo.test;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Decebal Suiu
 */
public class AvailablePortFinder {

    /**
     * Avoid returning privileged port.
     */
    public static final int MIN_PORT_NUMBER = 1100;

    public static final int MAX_PORT_NUMBER = 49151;

    public static int findAvailablePort() {
        return findAvailablePort(MIN_PORT_NUMBER, MAX_PORT_NUMBER);
    }

    public static int findAvailablePort(int minPortNumber, int maxPortNumber) {
        for (int port = minPortNumber; port < maxPortNumber; port++) {
            try {
                new ServerSocket(port).close();
                return port;
            } catch (IOException e) {
                // must already be taken
            }
        }

        throw new IllegalStateException("Could not find available port in range " + minPortNumber + " to " + maxPortNumber);
    }

}
