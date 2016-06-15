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
package ro.pippo.session.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import ro.pippo.core.PippoRuntimeException;

/**
 * Hazelcast Singleton Resource.
 *
 * @author Herman Barrantes
 */
public class HazelcastSingleton {

    private static HazelcastInstance instance;

    private HazelcastSingleton() {
        throw new PippoRuntimeException("You can't make a instance of singleton.");
    }

    public static HazelcastInstance getInstance() {
        if (instance == null) {
            instance = Hazelcast.newHazelcastInstance();
        }
        return instance;
    }
}
