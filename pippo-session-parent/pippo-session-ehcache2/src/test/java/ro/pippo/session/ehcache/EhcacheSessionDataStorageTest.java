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
package ro.pippo.session.ehcache;

import net.sf.ehcache.CacheManager;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import ro.pippo.session.SessionData;

/**
 * @author Herman Barrantes
 */
public class EhcacheSessionDataStorageTest {

    private static final String SESSION_NAME = "session";
    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";

    @AfterClass
    public static void tearDownClass() {
        CacheManager.getInstance().shutdown();
    }

    /**
     * Test of create method, of class EhcacheSessionDataStorage.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        EhcacheSessionDataStorage instance = new EhcacheSessionDataStorage();
        SessionData sessionData = instance.create();
        sessionData.put(KEY, VALUE);
        assertNotNull(sessionData);
        assertNotNull(sessionData.getId());
        assertNotNull(sessionData.getCreationTime());
        assertEquals(sessionData.get(KEY), VALUE);
    }

    /**
     * Test of save method, of class EhcacheSessionDataStorage.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        EhcacheSessionDataStorage instance = new EhcacheSessionDataStorage();
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.put(KEY, VALUE);
        instance.save(sessionData);
        SessionData saved = (SessionData) CacheManager
                .getInstance()
                .getCache(SESSION_NAME)
                .get(sessionId)
                .getObjectValue();
        assertEquals(sessionData, saved);
    }

    /**
     * Test of get method, of class EhcacheSessionDataStorage.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        EhcacheSessionDataStorage instance = new EhcacheSessionDataStorage();
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
     * Test of get method, of class EhcacheSessionDataStorage.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testGetExpired() throws InterruptedException {
        System.out.println("get expired");
        EhcacheSessionDataStorage instance = new EhcacheSessionDataStorage();
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.put(KEY, VALUE);
        instance.save(sessionData);
        Thread.sleep(2000L); // 2seconds
        SessionData deleted = instance.get(sessionId);
        assertNull(deleted);
    }

    /**
     * Test of delete method, of class EhcacheSessionDataStorage.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        EhcacheSessionDataStorage instance = new EhcacheSessionDataStorage();
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.put(KEY, VALUE);
        instance.save(sessionData);
        instance.delete(sessionId);
        SessionData deleted = instance.get(sessionId);
        assertNull(deleted);
    }

}
