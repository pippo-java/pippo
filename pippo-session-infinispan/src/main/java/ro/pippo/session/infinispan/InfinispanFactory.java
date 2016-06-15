/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.session.infinispan;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.PippoRuntimeException;

/**
 * Utility to build the cache manager.
 *
 * @author Herman Barrantes
 */
public class InfinispanFactory {

    private static final Logger log = LoggerFactory.getLogger(InfinispanFactory.class);
    private static final String DEFAULT_CONFIG_FILE = "infinispan.xml";

    private InfinispanFactory() {
        throw new PippoRuntimeException("You can't make a instance of factory.");
    }

    /**
     * Create cache manager with custom idle time.
     *
     * @param idleTime idle time in seconds
     * @return cache manager
     */
    public static EmbeddedCacheManager create(long idleTime) {
        Configuration configuration = new ConfigurationBuilder()
                .expiration().maxIdle(idleTime, TimeUnit.SECONDS)
                .build();
        return new DefaultCacheManager(configuration);
    }

    /**
     * Create cache manager with default config in "infinispan.xml" file.
     *
     * @return cache manager
     */
    public static EmbeddedCacheManager create() {
        return create(DEFAULT_CONFIG_FILE);
    }

    /**
     * Create cache manager with custom config file.
     *
     * @param file config file
     * @return cache manager
     */
    public static EmbeddedCacheManager create(String file) {
        try {
            return new DefaultCacheManager(file);
        } catch (IOException ex) {
            log.error("", ex);
            throw new PippoRuntimeException(ex);
        }
    }
}
