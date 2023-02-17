/*
 * Copyright (C) 2016-present the original author or authors.
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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * @author Decebal Suiu
 */
public class GZipResponseStream extends ServletOutputStream {

    private HttpServletResponse response;

    private ByteArrayOutputStream byteArrayOutputStream;
    private GZIPOutputStream gzipOutputStream;
    private boolean closed;

    public GZipResponseStream(HttpServletResponse response) throws IOException {
        super();

        this.response = response;

        byteArrayOutputStream = new ByteArrayOutputStream();
        gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            throw new IOException("This output stream has already been closed");
        }
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            gzipOutputStream.finish();

            byte[] bytes = byteArrayOutputStream.toByteArray();

            response.addHeader("Content-Length", Integer.toString(bytes.length));
            response.addHeader("Content-Encoding", "gzip");

            outputStream.write(bytes);
            outputStream.flush();
        } finally {
            gzipOutputStream.close();
            closed = true;
        }
    }

    @Override
    public void flush() throws IOException {
        if (closed) {
            throw new IOException("Cannot flush a closed output stream");
        }

        gzipOutputStream.flush();
    }

    @Override
    public void write(int b) throws IOException {
        if (closed) {
            throw new IOException("Cannot write to a closed output stream");
        }

        gzipOutputStream.write((byte) b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Cannot write to a closed output stream");
        }

        gzipOutputStream.write(b, off, len);
    }

    @Override
    public boolean isReady() {
        // we are writing to internal buffer, so we are always ready
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        // we are always ready, ignore
    }

}
