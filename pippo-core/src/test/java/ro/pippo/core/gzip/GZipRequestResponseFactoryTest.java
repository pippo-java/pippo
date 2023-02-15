/*
 * Copyright (C) 2018-present the original author or authors.
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
package ro.pippo.core.gzip;

import org.junit.Test;
import org.mockito.Mockito;
import ro.pippo.core.Application;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GZipRequestResponseFactoryTest {

    @Test
    public void testAcceptGzipEncoding() {
        GZipRequestResponseFactory requestResponseFactory = new GZipRequestResponseFactory(new Application());
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        // Accept-Encoding not specified
        Mockito.doReturn(null).when(httpServletRequest).getHeader("accept-encoding");
        assertFalse(requestResponseFactory.acceptsGZipEncoding(httpServletRequest));

        // Accept-Encoding specified
        Mockito.doReturn("gzip,deflate,identity").when(httpServletRequest).getHeader("accept-encoding");
        assertTrue(requestResponseFactory.acceptsGZipEncoding(httpServletRequest));

        // Explicit Accept-Encoding:*
        Mockito.doReturn("*").when(httpServletRequest).getHeader("accept-encoding");
        assertTrue(requestResponseFactory.acceptsGZipEncoding(httpServletRequest));

        // No Gzip
        Mockito.doReturn("deflate,identity").when(httpServletRequest).getHeader("accept-encoding");
        assertFalse(requestResponseFactory.acceptsGZipEncoding(httpServletRequest));
    }
}
