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
package ro.pippo.session.ehcache;

import java.net.URL;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import ro.pippo.core.PippoRuntimeException;

/**
 * Utility to build the cache manager.
 *
 * @author Herman Barrantes
 */
public class EhcacheFactory {

    private static final String DEFAULT_EHCACHE_FILE = "ehcache.xml";

    private EhcacheFactory() {
        throw new PippoRuntimeException("You can't make a instance of factory.");
    }

    public static CacheManager create() {
        return create(DEFAULT_EHCACHE_FILE);
    }

    public static CacheManager create(String path) {
        URL url = ClassLoader.getSystemResource(path);
        Configuration xmlConfig = new XmlConfiguration(url);
        CacheManager cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
        cacheManager.init();
        return cacheManager;
    }
}
