/*
 * Copyright (C) 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Decebal Suiu
 */
public class DefaultUriMatcherTest {

    private DefaultUriMatcher uriMatcher;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        uriMatcher = new DefaultUriMatcher();
    }

    @After
    public void after() {
        uriMatcher = null;
    }

    @Test
    public void testPathParams() {
        uriMatcher.addUriPattern("/contact/{id}");
        Map<String, String> params = uriMatcher.match("/contact/3", "/contact/{id}");
        assertNotNull(params);
        assertEquals(params.size(), 1);
        assertEquals(params.get("id"), "3");
    }

    @Test
    public void testNoBinding() {
        thrown.expect(PippoRuntimeException.class);
        thrown.expectMessage("No binding for '/contact/{id}'. Create binding with 'addUriPattern'");
        uriMatcher.match("/contact/1", "/contact/{id}");
    }

    @Test
    public void testMatch() throws Exception {
        uriMatcher.addUriPattern("/login");
        Map<String, String> params = uriMatcher.match("/login", "/login");
        assertNotNull(params);
        assertTrue(params.isEmpty());
    }

}
