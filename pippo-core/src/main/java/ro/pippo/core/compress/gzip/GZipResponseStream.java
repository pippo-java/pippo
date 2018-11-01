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
package ro.pippo.core.compress.gzip;

import ro.pippo.core.compress.deflate.DeflaterResponseStream;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * @author Decebal Suiu
 */
public class GZipResponseStream extends DeflaterResponseStream {

    private static final String CONTENT_ENCODING = "gzip";

    public GZipResponseStream(HttpServletResponse response) throws IOException {
        super(response);
    }

    @Override
    public void init() throws IOException {
        byteArrayOutputStream = new ByteArrayOutputStream();
        deflaterOutputStream = new GZIPOutputStream(byteArrayOutputStream);
    }

    @Override
    public String getContentEncoding() {
        return CONTENT_ENCODING;
    }
}
