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
package ro.pippo.core.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author Decebal Suiu
 */
public class IoUtils {

    /**
     * Copies all data from an InputStream to an OutputStream.
     *
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(InputStream input, OutputStream output) throws IOException {
        byte buffer[] = new byte[2 * 1024];
        long total = 0;
        int count;
        while ((count = input.read(buffer)) != -1) {
            output.write(buffer, 0, count);
            total += count;
        }

        return total;
    }

    public static String toString(InputStream input) throws IOException {
        StringWriter writer = new StringWriter();
        copy(input, writer);

        return writer.toString();
    }

    public static long copy(Reader reader, Writer writer) throws IOException {
        char[] buffer = new char[2 * 1024];
        long total = 0;
        int count;
        while ((count = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, count);
            total += count;
        }

        return total;
    }

    public static long copy(InputStream input, Writer writer) throws IOException {
        return copy(new InputStreamReader(input), writer);
    }

    public static long copy(InputStream input, File file) throws IOException {
        FileOutputStream output = new FileOutputStream(file);
        try {
            return copy(input, output);
        } finally {
            close(output);
        }
    }

    /**
     * Silently closes a Closeable.
     *
     * @return the exception or null if no exception thrown
     */
    public static IOException close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            return e;
        }

        return null;
    }

}
