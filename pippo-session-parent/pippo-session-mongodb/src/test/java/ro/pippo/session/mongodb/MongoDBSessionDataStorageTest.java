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
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ro.pippo.session.SessionData;

/**
 * @author Herman Barrantes
 */
public class MongoDBSessionDataStorageTest {

    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";
    private static final String DATABASE_NAME = "embedded";
    private static final String HOST = "localhost";
    private static MongodExecutable mongodExe;
    private static MongodProcess mongod;
    private static MongoClient mongoClient;

    @BeforeClass
    public static void setUpClass() throws IOException {
        Integer port = Network.getFreeServerPort();
        MongodStarter starter = MongodStarter.getDefaultInstance();
        mongodExe = starter.prepare(
                new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(port, Network.localhostIsIPv6()))
                .build());
        mongod = mongodExe.start();
        mongoClient = new MongoClient(HOST, port);
    }

    @AfterClass
    public static void tearDownClass() {
        mongoClient.close();
        mongod.stop();
        mongodExe.stop();
    }

    /**
     * Test of create method, of class MongoDBSessionDataStorage.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        MongoDBSessionDataStorage instance = new MongoDBSessionDataStorage(mongoClient.getDatabase(DATABASE_NAME));
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.put(KEY, VALUE);
        instance.save(sessionData);
        SessionData saved = instance.get(sessionId);
        assertEquals(sessionData, saved);
        String value1 = sessionData.get(KEY);
        String value2 = saved.get(KEY);
        assertEquals(value1, value2);
    }

    /**
     * Test of save method, of class MongoDBSessionDataStorage.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        MongoDBSessionDataStorage instance = new MongoDBSessionDataStorage(mongoClient.getDatabase(DATABASE_NAME));
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.put(KEY, VALUE);
        instance.save(sessionData);
        SessionData saved = instance.get(sessionId);
        assertEquals(sessionData, saved);
        String value1 = sessionData.get(KEY);
        String value2 = saved.get(KEY);
        assertEquals(value1, value2);
    }

    /**
     * Test of get method, of class MongoDBSessionDataStorage.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        MongoDBSessionDataStorage instance = new MongoDBSessionDataStorage(mongoClient.getDatabase(DATABASE_NAME));
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.put(KEY, VALUE);
        instance.save(sessionData);
        SessionData saved = instance.get(sessionId);
        assertEquals(sessionData, saved);
        String value1 = sessionData.get(KEY);
        String value2 = saved.get(KEY);
        assertEquals(value1, value2);
    }

    /**
     * Test of delete method, of class MongoDBSessionDataStorage.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        MongoDBSessionDataStorage instance = new MongoDBSessionDataStorage(mongoClient.getDatabase(DATABASE_NAME));
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.put(KEY, VALUE);
        instance.save(sessionData);
        instance.delete(sessionId);
        SessionData deleted = instance.get(sessionId);
        assertNull(deleted);
    }

}
