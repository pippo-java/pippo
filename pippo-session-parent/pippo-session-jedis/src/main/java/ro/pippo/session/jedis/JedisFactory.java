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
package ro.pippo.session.jedis;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;

/**
 * Utility to build the client.
 *
 * @author Herman Barrantes
 */
public class JedisFactory {

    private static final String HOST = "redis.host";
    private static final String MIN_IDLE = "redis.minIdle";
    private static final String MAX_IDLE = "redis.maxIdle";
    private static final String MAX_TOTAL = "redis.maxTotal";

    private JedisFactory() {
        throw new PippoRuntimeException("You can't make a instance of factory.");
    }

    /**
     * Create a Jedis poll with pippo settings. URL format:
     * 'redis://[:password@]host[:port][/db-number][?option=value]'
     *
     * @param settings pippo settings
     * @return Jedis pool
     */
    public static JedisPool create(final PippoSettings settings) {
        String host = settings.getString(HOST, Protocol.DEFAULT_HOST).trim();
        int minIdle = settings.getInteger(MIN_IDLE, GenericObjectPoolConfig.DEFAULT_MIN_IDLE);
        int maxIdle = settings.getInteger(MAX_IDLE, GenericObjectPoolConfig.DEFAULT_MAX_IDLE);
        int maxTotal = settings.getInteger(MAX_TOTAL, GenericObjectPoolConfig.DEFAULT_MAX_TOTAL);
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(minIdle);
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxTotal);

        try {
            return new JedisPool(config, new URI(host));
        } catch (URISyntaxException e) {
            throw new PippoRuntimeException("Malformed redis URI", e);
        }
    }

}
