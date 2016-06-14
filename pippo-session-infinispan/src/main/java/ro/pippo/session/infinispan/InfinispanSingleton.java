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

import java.io.IOException;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.PippoRuntimeException;

/**
 *
 * @author Herman Barrantes
 * @since 06/06/2016
 */
public class InfinispanSingleton {

    private static final Logger log = LoggerFactory.getLogger(InfinispanSingleton.class);
    private static final String DEFAULT_CONFIG_FILE = "infinispan.xml";
    private static EmbeddedCacheManager instance;

    private InfinispanSingleton() {
        throw new PippoRuntimeException("You can't make a instance of singleton.");
    }

    protected static EmbeddedCacheManager getInstance() {
        if (instance == null) {
            try {
                instance = new DefaultCacheManager(DEFAULT_CONFIG_FILE);
            } catch (IOException ex) {
                log.error("Error on load '{0}' configuration file", DEFAULT_CONFIG_FILE, ex);
                throw new PippoRuntimeException(ex);
            }
        }
        return instance;
    }

}
