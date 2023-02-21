/*
 * Copyright 2016-present the original author or authors.
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
package ro.pippo.session.xmemcached;

import de.flapdoodle.embed.memcached.MemcachedExecutable;
import de.flapdoodle.embed.memcached.MemcachedProcess;
import de.flapdoodle.embed.memcached.MemcachedStarter;
import de.flapdoodle.embed.memcached.config.MemcachedConfig;
import de.flapdoodle.embed.memcached.distribution.Version;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ro.pippo.session.SessionData;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Herman Barrantes
 */
@Disabled("See issue #337 on GitHub")
public class XmemcachedSessionDataStorageTest {

    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";
    private static final int IDLE_TIME = 1;
    private static final String HOST = "localhost";
    private static final Integer PORT = 11211;
    private static MemcachedExecutable memcachedExe;
    private static MemcachedProcess memcached;
    private static MemcachedClient client;

    @BeforeAll
    public static void setUpClass() throws IOException {
        MemcachedStarter runtime = MemcachedStarter.getDefaultInstance();
        memcachedExe = runtime.prepare(
                new MemcachedConfig(Version.Main.PRODUCTION, PORT));
        memcached = memcachedExe.start();
        client = new XMemcachedClient(new InetSocketAddress(HOST, PORT));
    }

    @AfterAll
    public static void tearDownClass() throws IOException {
        client.shutdown();
        memcached.stop();
        memcachedExe.stop();
    }

    /**
     * Test of create method, of class XmemcachedSessionDataStorage.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        XmemcachedSessionDataStorage instance = new XmemcachedSessionDataStorage(client, IDLE_TIME);
        SessionData sessionData = instance.create();
        sessionData.put(KEY, VALUE);
        assertNotNull(sessionData);
        assertNotNull(sessionData.getId());
        assertNotNull(sessionData.getCreationTime());
        assertEquals(sessionData.get(KEY), VALUE);
    }

    /**
     * Test of save method, of class XmemcachedSessionDataStorage.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        XmemcachedSessionDataStorage instance = new XmemcachedSessionDataStorage(client, IDLE_TIME);
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
     * Test of get method, of class XmemcachedSessionDataStorage.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        XmemcachedSessionDataStorage instance = new XmemcachedSessionDataStorage(client, IDLE_TIME);
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
     * Test of get method, of class XmemcachedSessionDataStorage.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testGetExpired() throws InterruptedException {
        System.out.println("get expired");
        XmemcachedSessionDataStorage instance = new XmemcachedSessionDataStorage(client, IDLE_TIME);
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.put(KEY, VALUE);
        instance.save(sessionData);
        Thread.sleep(2000L); // 2seconds
        SessionData deleted = instance.get(sessionId);
        assertNull(deleted);
    }

    /**
     * Test of delete method, of class XmemcachedSessionDataStorage.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        XmemcachedSessionDataStorage instance = new XmemcachedSessionDataStorage(client, IDLE_TIME);
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.put(KEY, VALUE);
        instance.save(sessionData);
        instance.delete(sessionId);
        SessionData deleted = instance.get(sessionId);
        assertNull(deleted);
    }

}
