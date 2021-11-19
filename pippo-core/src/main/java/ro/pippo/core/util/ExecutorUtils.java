/*
 * Copyright (C) 2017-present the original author or authors.
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

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author Decebal Suiu
 */
public class ExecutorUtils {

    public static ThreadFactory defaultThreadFactoryWithPrefix(String prefix) {
        return defaultThreadFactoryWithPrefix(prefix, false);
    }

    public static ThreadFactory defaultThreadFactoryWithPrefix(String prefix, boolean daemon) {
        return new PrefixingDefaultThreadFactory(prefix, daemon);
    }

    private static class PrefixingDefaultThreadFactory implements ThreadFactory {

        private final String prefix;
        private final ThreadFactory defaultThreadFactory;
        private final boolean daemon;

        public PrefixingDefaultThreadFactory(String prefix, boolean daemon) {
            this.defaultThreadFactory = Executors.defaultThreadFactory();
            this.prefix = prefix;
            this.daemon = daemon;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = defaultThreadFactory.newThread(r);
            thread.setName(prefix + thread.getName());
            thread.setDaemon(daemon);

            return thread;
        }

    }

}
