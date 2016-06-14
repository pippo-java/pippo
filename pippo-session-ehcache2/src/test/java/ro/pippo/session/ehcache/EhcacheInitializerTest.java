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
package ro.pippo.session.ehcache;

import net.sf.ehcache.CacheManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ro.pippo.core.Application;

/**
 *
 * @author Herman Barrantes
 */
public class EhcacheInitializerTest {
    
    private static final String SESSION_NAME = "session";
    private static Application application;
    
    @BeforeClass
    public static void setUpClass() {
        application = new Application();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of init method, of class EhcacheInitializer.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        EhcacheInitializer instance = new EhcacheInitializer();
        instance.init(application);
        assertTrue(CacheManager.getInstance().cacheExists(SESSION_NAME));
    }

    /**
     * Test of destroy method, of class EhcacheInitializer.
     */
    @Test
    public void testDestroy() {
        EhcacheInitializer instance = new EhcacheInitializer();
        instance.destroy(application);
    }
    
}
