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

import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import ro.pippo.core.Application;

/**
 *
 * @author Herman Barrantes
 * @since 02/06/2016
 */
@Ignore("You need run MemCached localy")
public class SpymemcachedFactoryTest {

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
     * Test of create method, of class SpymemcachedUtil.
     */
    @Test
    public void testCreate_PippoSettings() {
        System.out.println("create");
        MemcachedClient result = SpymemcachedFactory.create(application.getPippoSettings());
        assertNotNull(result);
        result.shutdown();
    }

    /**
     * Test of create method, of class SpymemcachedUtil.
     */
    @Test
    public void testCreate_5args() {
        System.out.println("create");
        String hosts = "localhost:11211";
        ConnectionFactoryBuilder.Protocol protocol = ConnectionFactoryBuilder.Protocol.BINARY;
        String user = "";
        String pass = "";
        String[] authMechanisms = new String[]{"PLAIN"};
        MemcachedClient result = SpymemcachedFactory.create(hosts, protocol, user, pass, authMechanisms);
        assertNotNull(result);
        result.shutdown();
    }

}
