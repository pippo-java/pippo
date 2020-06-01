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
package ro.pippo.core.gzip;

import ro.pippo.core.util.IoUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author Decebal Suiu
 */
public class GZipResponseWrapper extends HttpServletResponseWrapper {

    private HttpServletResponse response;
    private ServletOutputStream stream;
    private PrintWriter writer;

    public GZipResponseWrapper(HttpServletResponse response) {
        super(response);

        this.response = response;
    }

    @Override
    public void flushBuffer() throws IOException {
        stream.flush();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called");
        }

        if (stream == null) {
            stream = createOutputStream();
        }

        return stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer != null) {
            return writer;
        }

        if (stream != null) {
            throw new IllegalStateException("getOutputStream() has already been called");
        }

        stream = createOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));

        return writer;
    }

    @Override
    public void setContentLength(int length) {
        // do nothing
    }

    public void finish() {
        IoUtils.close(writer);
        IoUtils.close(stream);
    }

    private ServletOutputStream createOutputStream() throws IOException {
        return new GZipResponseStream(response);
    }

}
