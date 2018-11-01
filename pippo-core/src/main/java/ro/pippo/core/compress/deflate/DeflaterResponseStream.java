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
package ro.pippo.core.compress.deflate;

import ro.pippo.core.HttpConstants;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

public class DeflaterResponseStream extends ServletOutputStream {

    private static final String CONTENT_ENCODING = "deflate";

    private HttpServletResponse response;
    public ByteArrayOutputStream byteArrayOutputStream;
    public DeflaterOutputStream deflaterOutputStream;
    private boolean closed;

    public DeflaterResponseStream(HttpServletResponse response) throws IOException {
        super();
        this.response = response;
        init();
    }

    public void init() throws IOException {
        byteArrayOutputStream = new ByteArrayOutputStream();
        deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream);
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            throw new IOException("This output stream has already been closed");
        }

        deflaterOutputStream.finish();

        byte[] bytes = byteArrayOutputStream.toByteArray();

        response.addHeader(HttpConstants.Header.CONTENT_LENGTH, Integer.toString(bytes.length));
        response.addHeader(HttpConstants.Header.CONTENT_ENCODING, getContentEncoding());

        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();

        closed = true;
    }

    @Override
    public void flush() throws IOException {
        if (closed) {
            throw new IOException("Cannot flush a closed output stream");
        }

        deflaterOutputStream.flush();
    }

    @Override
    public void write(int b) throws IOException {
        if (closed) {
            throw new IOException("Cannot write to a closed output stream");
        }

        deflaterOutputStream.write((byte) b);
    }

    @Override
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Cannot write to a closed output stream");
        }

        deflaterOutputStream.write(b, off, len);
    }

    public String getContentEncoding() {
        return CONTENT_ENCODING;
    }

}
