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
package ro.pippo.session.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;

/**
 * Utility to build the client.
 *
 * @author Herman Barrantes
 */
public class MongoDBFactory {

    private static final String HOST = "mongodb.hosts";

    private MongoDBFactory() {
        throw new PippoRuntimeException("You can't make a instance of factory.");
    }

    /**
     * Create a MongoDB client with pippo settings.
     *
     * @param settings pippo settings
     * @return MongoDB client
     */
    public static MongoClient create(final PippoSettings settings) {
        String host = settings.getString(HOST, "mongodb://localhost:27017");
        return create(host);
    }

    /**
     * Create a MongoDB client with params.
     *
     * @param hosts list of hosts of the form
     * "mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]"
     * @return MongoDB client
     * @see https://docs.mongodb.com/manual/reference/connection-string/
     */
    public static MongoClient create(String hosts) {
        MongoClientURI connectionString = new MongoClientURI(hosts);
        return new MongoClient(connectionString);
    }
}
