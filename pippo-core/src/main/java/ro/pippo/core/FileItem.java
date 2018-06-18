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

import ro.pippo.core.util.IoUtils;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a file from an HTTP multipart/form-data request.
 *
 * @author Decebal Suiu
 */
public class FileItem {

    private Part part;
    private String submittedFileName;

    public FileItem(Part part) {
        this.part = part;
    }

    /**
     * Gets the name of this part
     *
     * @return The name of this part as a <tt>String</tt>
     */
    public String getName() {
        return  part.getName();
    }

    /**
     * Retrieves the filename specified by the client.
     * @return
     */
    public String getSubmittedFileName() {
        // TODO this method also introduced in servlet 3.1 specification (delegate to part when I adopt servlet 3.1)
        if (submittedFileName == null) {
            String header = part.getHeader(HttpConstants.Header.CONTENT_DISPOSITION);
            if (header == null) {
                return null;
            }

            for (String headerPart : header.split(";")) {
                if (headerPart.trim().startsWith("filename")) {
                    submittedFileName = headerPart.substring(headerPart.indexOf('=') + 1).trim().replace("\"", "");
                    break;
                }
            }
        }

        return submittedFileName;
    }

    /**
     * Returns the size of this file.
     *
     * @return a <code>long</code> specifying the size of this part, in bytes.
     */
    public long getSize() {
        return part.getSize();
    }

    /**
     * Gets the content type of this part.
     *
     * @return The content type of this part.
     */
    public String getContentType() {
        return  part.getContentType();
    }

    /**
     * Gets the content of this part as an <tt>InputStream</tt>
     *
     * @return The content of this file as an <tt>InputStream</tt>
     * @throws IOException If an error occurs in retrieving the content
     * as an <tt>InputStream</tt>
     */
    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    /**
     * A convenience method to write this uploaded item to disk.
     *
     * @param fileName the name of the file to which the stream will be
     * written. The file is created relative to the location as
     * specified in the Application
     *
     * @throws IOException if an error occurs.
     */
    public void write(String fileName) throws IOException {
        part.write(fileName);
    }

    /**
     * Saves this file item to a given file on the server side.
     *
     * @param file
     * @throws IOException
     */
    public void write(File file) throws IOException {
        try (InputStream inputStream = getInputStream()) {
            IoUtils.copy(inputStream, file);
        }
    }

    /**
     * Deletes the underlying storage for a file item, including deleting any
     * associated temporary disk file.
     *
     * @throws IOException if an error occurs.
     */
    public void delete() throws IOException {
        part.delete();
    }

    @Override
    public String toString() {
        return "FileItem{" +
                "name='" + getName() + '\'' +
                ", submittedFileName='" + getSubmittedFileName() + '\'' +
                ", size=" + getSize() +
                ", contentType='" + getContentType() + '\'' +
                '}';
    }

}
