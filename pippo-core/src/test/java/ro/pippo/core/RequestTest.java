/*
 * Copyright (C) 2016 the original author or authors.
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

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Decebal Suiu
 */
public class RequestTest {

    @Test
    public void parameterTest() {
        // mock servlet request
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getParameterNames()).thenReturn(Collections.enumeration(Collections.singletonList("name")));
//        when(servletRequest.getParameterValues("name")).thenReturn(new String[] { "Возера Радасці" } );
        when(servletRequest.getParameterValues("name")).thenReturn(new String[] { "métier" });

        Application application = new Application();
        Request request = new Request(servletRequest, application);
        String name = request.getParameter("name").toString();
//        assertEquals(name, "Возера Радасці");
        assertEquals(name, "métier");
    }

    @Test
    public void createEntityTest() {
        // mock servlet request
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getParameterNames()).thenReturn(Collections.enumeration(Collections.singletonList("name")));
//        when(servletRequest.getParameterValues("name")).thenReturn(new String[] { "Возера Радасці" } );
        when(servletRequest.getParameterValues("name")).thenReturn(new String[] { "métier" });

        Application application = new Application();
        Request request = new Request(servletRequest, application);
        User user = request.createEntityFromParameters(User.class);
        assertNotNull(user);
        String name = user.getName();
//        assertEquals(name, "Возера Радасці");
        assertEquals(name, "métier");
    }

    public static class User {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}
