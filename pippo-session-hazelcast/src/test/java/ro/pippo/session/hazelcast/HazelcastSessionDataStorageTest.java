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
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.pippo.session.SessionData;

/**
 *
 * @author Herman Barrantes
 */
public class HazelcastSessionDataStorageTest {

    private static final String SESSION_NAME = "session";
    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";
    private static HazelcastInstance hazelcastInstance;
    private static HazelcastInstance hazelcastInstance2;

    @BeforeClass
    public static void setUpClass() {
        hazelcastInstance = Hazelcast.newHazelcastInstance();
        hazelcastInstance2 = Hazelcast.newHazelcastInstance();
    }

    @AfterClass
    public static void tearDownClass() {
        hazelcastInstance.shutdown();
        hazelcastInstance2.shutdown();
    }

    /**
     * Test of create method, of class HazelcastSessionDataStorage.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        HazelcastSessionDataStorage instance = new HazelcastSessionDataStorage(hazelcastInstance);
        SessionData sessionData = instance.create();
        sessionData.setAttribute(KEY, VALUE);
        assertNotNull(sessionData);
        assertNotNull(sessionData.getId());
        assertNotNull(sessionData.getCreationTime());
        assertEquals(sessionData.getAttribute(KEY), VALUE);
    }

    /**
     * Test of save method, of class HazelcastSessionDataStorage.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        HazelcastSessionDataStorage instance = new HazelcastSessionDataStorage(hazelcastInstance);
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.setAttribute(KEY, VALUE);
        instance.save(sessionData);
        SessionData saved = hazelcastInstance2
                .<String, SessionData>getMap(SESSION_NAME)
                .get(sessionId);
        assertEquals(sessionData, saved);
    }

    /**
     * Test of get method, of class HazelcastSessionDataStorage.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        HazelcastSessionDataStorage instance = new HazelcastSessionDataStorage(hazelcastInstance);
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.setAttribute(KEY, VALUE);
        instance.save(sessionData);
        SessionData saved = instance.get(sessionId);
        assertEquals(sessionData, saved);
        assertEquals(sessionData.getAttribute(KEY), saved.getAttribute(KEY));
    }

    /**
     * Test of get method, of class HazelcastSessionDataStorage.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testGetExpired() throws InterruptedException {
        System.out.println("get expired");
        HazelcastSessionDataStorage instance = new HazelcastSessionDataStorage(hazelcastInstance);
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.setAttribute(KEY, VALUE);
        instance.save(sessionData);
        Thread.sleep(2000L); // 2seconds
        SessionData deleted = instance.get(sessionId);
        assertNull(deleted);
    }

    /**
     * Test of delete method, of class HazelcastSessionDataStorage.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        HazelcastSessionDataStorage instance = new HazelcastSessionDataStorage(hazelcastInstance);
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.setAttribute(KEY, VALUE);
        instance.save(sessionData);
        instance.delete(sessionId);
        SessionData deleted = instance.get(sessionId);
        assertNull(deleted);
    }

}
