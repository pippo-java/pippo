/*
 * Copyright (C) 2017 the original author or authors.
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

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author munendrasn
 */
public class ResponseTest {

    @Test
    public void testResetResponse() {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        doNothing().when(servletResponse).setCharacterEncoding(StandardCharsets.UTF_8.toString());
        doNothing().when(servletResponse).reset();
        doReturn(false).when(servletResponse).isCommitted();
        doNothing().when(servletResponse).setStatus(500);

        Response response = new Response(servletResponse, new Application());
        response.header("content-type", "application/json");
        response.internalError();
        response.cookie("user", "pippo");

        assertFalse(response.getCookies().isEmpty());
        assertNotNull(response.getHeader("content-type"));
        assertEquals(500, response.getStatus());

        response.reset();

        assertTrue(response.getCookies().isEmpty());
        assertNull(response.getHeader("content-type"));
        assertNotEquals(500, response.getStatus());
    }

}
