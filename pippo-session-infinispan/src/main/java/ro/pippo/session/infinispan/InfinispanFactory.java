/*
 * Copyright 2016 Herman Barrantes.
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

import java.util.concurrent.TimeUnit;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import ro.pippo.core.PippoRuntimeException;

/**
 *
 * @author Herman Barrantes
 * @since 06/06/2016
 */
public class InfinispanFactory {

    private InfinispanFactory() {
        throw new PippoRuntimeException("You can't make a instance of factory.");
    }

    public static EmbeddedCacheManager create(long idleTime) {
        Configuration configuration = new ConfigurationBuilder()
                .expiration().maxIdle(idleTime, TimeUnit.SECONDS)
                .build();
        return new DefaultCacheManager(configuration);
    }
}
