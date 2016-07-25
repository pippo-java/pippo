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
package ro.pippo.session.jdbc;

//import com.mchange.v2.c3p0.ComboPooledDataSource;
//import java.beans.PropertyVetoException;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.pippo.session.SessionData;

/**
 * @author Herman Barrantes
 */
public class JDBCSessionDataStorageTest {

    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";
    private static BasicDataSource dataSource;

    // With C3P0
//    @BeforeClass
//    public static void setUpClass() {
//        ComboPooledDataSource cpds = new ComboPooledDataSource();
//        try {
//            cpds.setDriverClass("org.h2.Driver");
//        } catch (PropertyVetoException ex) {
//        }
//        cpds.setJdbcUrl("jdbc:h2:mem:test;INIT=runscript from 'src/test/resources/create.sql'");
//        cpds.setUser("sa");
//        cpds.setPassword("sa");
//        dataSource = cpds;
//    }

    // With DBCP2
    @BeforeClass
    public static void setUpClass() {
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("org.h2.Driver");
        bds.setUrl("jdbc:h2:mem:test;INIT=runscript from 'src/test/resources/create.sql'");
        bds.setUsername("sa");
        bds.setPassword("sa");
        dataSource = bds;
    }

    // With JNDI
//    @BeforeClass
//    public static void setUpClass() {
//        Context initialContext = new InitialContext();
//        DataSource datasource = (DataSource)initialContext.lookup("jdbc/test");
//        dataSource = bds;
//    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        dataSource.close();
    }

    /**
     * Test of create method, of class JDBCSessionDataStorage.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        JDBCSessionDataStorage instance = new JDBCSessionDataStorage(dataSource);
        SessionData sessionData = instance.create();
        sessionData.put(KEY, VALUE);
        assertNotNull(sessionData);
        assertNotNull(sessionData.getId());
        assertNotNull(sessionData.getCreationTime());
        assertEquals(sessionData.get(KEY), VALUE);
    }

    /**
     * Test of save method, of class JDBCSessionDataStorage.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        JDBCSessionDataStorage instance = new JDBCSessionDataStorage(dataSource);
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
     * Test of get method, of class JDBCSessionDataStorage.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        JDBCSessionDataStorage instance = new JDBCSessionDataStorage(dataSource);
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
     * Test of delete method, of class JDBCSessionDataStorage.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        JDBCSessionDataStorage instance = new JDBCSessionDataStorage(dataSource);
        SessionData sessionData = instance.create();
        String sessionId = sessionData.getId();
        sessionData.put(KEY, VALUE);
        instance.save(sessionData);
        instance.delete(sessionId);
        SessionData deleted = instance.get(sessionId);
        assertNull(deleted);
    }

}
