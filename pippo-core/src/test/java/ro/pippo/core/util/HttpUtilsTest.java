/*
 * Copyright (C) 2018 the original author or authors.
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
package ro.pippo.core.util;

import org.junit.Test;
import org.mockito.Mockito;
import ro.pippo.core.compress.CompressedResponseWrapper;
import ro.pippo.core.compress.deflate.DeflaterResponseWrapper;
import ro.pippo.core.compress.gzip.GZipResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import static org.junit.Assert.*;

public class HttpUtilsTest {

    @Test
    public void getHttpResponseWrapper() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);


        // Accept-Encoding not specified
        Mockito.doReturn(null).when(request).getHeader("accept-encoding");
        HttpServletResponseWrapper responseWrapper = HttpUtils.getHttpResponseWrapper(response, request);
        assertFalse(responseWrapper instanceof CompressedResponseWrapper);

        // Accept-Encoding specified
        Mockito.doReturn("gzip,deflate,identity").when(request).getHeader("accept-encoding");
        responseWrapper = HttpUtils.getHttpResponseWrapper(response, request);
        assertTrue(responseWrapper instanceof GZipResponseWrapper);

        // Explicit Accept-Encoding:*
        Mockito.doReturn("*").when(request).getHeader("accept-encoding");
        responseWrapper = HttpUtils.getHttpResponseWrapper(response, request);
        assertTrue(responseWrapper instanceof GZipResponseWrapper);

        // No Gzip but Deflate
        Mockito.doReturn("deflate,identity").when(request).getHeader("accept-encoding");
        responseWrapper = HttpUtils.getHttpResponseWrapper(response, request);
        assertTrue(responseWrapper instanceof DeflaterResponseWrapper);

        // identity
        Mockito.doReturn("identity").when(request).getHeader("accept-encoding");
        responseWrapper = HttpUtils.getHttpResponseWrapper(response, request);
        assertFalse(responseWrapper instanceof CompressedResponseWrapper);
    }
}
