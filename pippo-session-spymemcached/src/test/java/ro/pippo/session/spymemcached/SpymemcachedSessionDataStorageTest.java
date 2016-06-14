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
package ro.pippo.session.spymemcached;

import net.spy.memcached.MemcachedClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import ro.pippo.core.Application;
import ro.pippo.session.SessionData;

/**
 *
 * @author Herman Barrantes
 * @since 02/06/2016
 */
@Ignore("You need run MemCached localy")
public class SpymemcachedSessionDataStorageTest {

    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";
    private static final int IDLE_TIME = 1;
    private static Application application;
    private static MemcachedClient client;

    @BeforeClass
    public static void setUpClass() {
        application = new Application();
        client = SpymemcachedFactory.create(application.getPippoSettings());
    }

    @AfterClass
    public static void tearDownClass() {
        client.shutdown();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of create method, of class SpymemcachedSessionDataStorage.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        SpymemcachedSessionDataStorage instance = new SpymemcachedSessionDataStorage(client, IDLE_TIME);
        SessionData sessionData = instance.create();
        sessionData.setAttribute(KEY, VALUE);
        assertNotNull(sessionData);
        assertNotNull(sessionData.getId());
        assertNotNull(sessionData.getCreationTime());
        assertEquals(sessionData.getAttribute(KEY), VALUE);
    }

    /**
     * Test of save method, of class SpymemcachedSessionDataStorage.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        SpymemcachedSessionDataStorage instance = new SpymemcachedSessionDataStorage(client, IDLE_TIME);
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.setAttribute(KEY, VALUE);
        instance.save(sessionData);
        SessionData saved = instance.get(sessionId);
        assertEquals(sessionData, saved);
        assertEquals(sessionData.getAttribute(KEY), saved.getAttribute(KEY));
    }

    /**
     * Test of get method, of class SpymemcachedSessionDataStorage.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        SpymemcachedSessionDataStorage instance = new SpymemcachedSessionDataStorage(client, IDLE_TIME);
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.setAttribute(KEY, VALUE);
        instance.save(sessionData);
        SessionData saved = instance.get(sessionId);
        assertEquals(sessionData, saved);
        assertEquals(sessionData.getAttribute(KEY), saved.getAttribute(KEY));
    }

    /**
     * Test of get method, of class SpymemcachedSessionDataStorage.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testGetExpired() throws InterruptedException {
        System.out.println("get expired");
        SpymemcachedSessionDataStorage instance = new SpymemcachedSessionDataStorage(client, IDLE_TIME);
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.setAttribute(KEY, VALUE);
        instance.save(sessionData);
        Thread.sleep(2000L); // 2seconds
        SessionData deleted = instance.get(sessionId);
        assertNull(deleted);
    }

    /**
     * Test of delete method, of class SpymemcachedSessionDataStorage.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        SpymemcachedSessionDataStorage instance = new SpymemcachedSessionDataStorage(client, IDLE_TIME);
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.setAttribute(KEY, VALUE);
        instance.save(sessionData);
        instance.delete(sessionId);
        SessionData deleted = instance.get(sessionId);
        assertNull(deleted);
    }

}