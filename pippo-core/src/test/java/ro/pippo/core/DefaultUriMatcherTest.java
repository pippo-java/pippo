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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
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

    @Test
    public void testUriForWithRegex() throws Exception {
        UriMatcher.UriPatternBinding binding = uriMatcher.addUriPattern("/user/{email}/{id: .*}");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", "test@test.com");
        parameters.put("id", 5);
        String path = uriMatcher.uriFor(binding.getUriPattern(), parameters);

        assertThat(path, equalTo("/user/test@test.com/5"));
    }

    @Test
    public void testUriForWithMultipleRegex() throws Exception {
        UriMatcher.UriPatternBinding binding = uriMatcher.addUriPattern("/user/{email: .*}/test/{id: .*}");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", "test@test.com");
        parameters.put("id", 5);
        String path = uriMatcher.uriFor(binding.getUriPattern(), parameters);

        assertThat(path, equalTo("/user/test@test.com/test/5"));
    }

    @Test
    public void testUriForWithSplat() throws Exception {
        UriMatcher.UriPatternBinding binding = uriMatcher.addUriPattern("/repository/{repo: .*}/ticket/{id: .*}");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("repo", "test/myrepo");
        parameters.put("id", 5);
        String path = uriMatcher.uriFor(binding.getUriPattern(), parameters);

        assertThat(path, equalTo("/repository/test/myrepo/ticket/5"));
    }

    @Test
    public void testUriForWithRegexAndQueryParameters() throws Exception {
        UriMatcher.UriPatternBinding binding = uriMatcher.addUriPattern("/user/{email}/{id: .*}");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", "test@test.com");
        parameters.put("id", 5);
        parameters.put("query", "recent_changes");
        String path = uriMatcher.uriFor(binding.getUriPattern(), parameters);

        assertThat(path, equalTo("/user/test@test.com/5?query=recent_changes"));
    }

    @Test
    public void testUriForWithEncodedParameters() throws Exception {
        UriMatcher.UriPatternBinding binding = uriMatcher.addUriPattern("/user/{email}");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", "test@test.com");
        parameters.put("name", "Decebal Suiu");
        String path = uriMatcher.uriFor(binding.getUriPattern(), parameters);

        assertThat(path, equalTo("/user/test@test.com?name=Decebal+Suiu"));
    }

}
