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
package ro.pippo.session.infinispan;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ro.pippo.core.Application;
import ro.pippo.session.SessionData;

/**
 *
 * @author Herman Barrantes
 */
public class InfinispanSessionDataStorageTest {

    private static final String SESSION_NAME = "session";
    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";
    private static Application application;
    private static InfinispanInitializer initializer;

    public InfinispanSessionDataStorageTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        application = new Application();
        initializer = new InfinispanInitializer();
        initializer.init(application);
    }

    @AfterClass
    public static void tearDownClass() {
        initializer.destroy(application);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of create method, of class InfinispanSessionDataStorage.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        InfinispanSessionDataStorage instance = new InfinispanSessionDataStorage();
        SessionData sessionData = instance.create();
        sessionData.setAttribute(KEY, VALUE);
        assertNotNull(sessionData);
        assertNotNull(sessionData.getId());
        assertNotNull(sessionData.getCreationTime());
        assertEquals(sessionData.getAttribute(KEY), VALUE);
    }

    /**
     * Test of save method, of class InfinispanSessionDataStorage.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        InfinispanSessionDataStorage instance = new InfinispanSessionDataStorage();
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.setAttribute(KEY, VALUE);
        instance.save(sessionData);
        SessionData saved = InfinispanSingleton
                .getInstance()
                .<String, SessionData>getCache(SESSION_NAME)
                .get(sessionId);
        assertEquals(sessionData, saved);
    }

    /**
     * Test of get method, of class InfinispanSessionDataStorage.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        InfinispanSessionDataStorage instance = new InfinispanSessionDataStorage();
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.setAttribute(KEY, VALUE);
        instance.save(sessionData);
        SessionData saved = instance.get(sessionId);
        assertEquals(sessionData, saved);
        assertEquals(sessionData.getAttribute(KEY), saved.getAttribute(KEY));
    }

    /**
     * Test of get method, of class InfinispanSessionDataStorage.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testGetExpired() throws InterruptedException {
        System.out.println("get expired");
        InfinispanSessionDataStorage instance = new InfinispanSessionDataStorage();
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.setAttribute(KEY, VALUE);
        instance.save(sessionData);
        Thread.sleep(2000L); // 2 seconds
        SessionData deleted = instance.get(sessionId);
        assertNull(deleted);
    }

    /**
     * Test of delete method, of class InfinispanSessionDataStorage.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        InfinispanSessionDataStorage instance = new InfinispanSessionDataStorage();
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.setAttribute(KEY, VALUE);
        instance.save(sessionData);
        instance.delete(sessionId);
        SessionData deleted = instance.get(sessionId);
        assertNull(deleted);
    }

}
