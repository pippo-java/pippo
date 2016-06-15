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
package ro.pippo.session.jcache;

import javax.cache.Caching;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.Initializer;

/**
 *
 * @author Herman Barrantes
 */
@MetaInfServices(Initializer.class)
public class JCacheInitializer implements Initializer {

    private static final Logger log = LoggerFactory.getLogger(JCacheInitializer.class);
    private static final String JCACHE_INITIALIZER_ENABLED = "jcache.initializer.enabled";
    private boolean enabled = true;

    @Override
    public void init(Application application) {
        enabled = application.getPippoSettings().getBoolean(JCACHE_INITIALIZER_ENABLED, true);
        if (enabled) {
            Caching.getCachingProvider().getCacheManager();
            log.debug("JCacheInitializer init");
        }
    }

    @Override
    public void destroy(Application application) {
        if (enabled) {
            Caching.getCachingProvider().getCacheManager().close();
            log.debug("JCacheInitializer destroy");
        }
    }

}
