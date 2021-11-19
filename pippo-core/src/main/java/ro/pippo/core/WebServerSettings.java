/*
 * Copyright (C) 2014-present the original author or authors.
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

import java.io.Serializable;

/**
 * @author Decebal Suiu
 */
public class WebServerSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_PORT = 8338;

    private String host = "localhost";
    private int port = DEFAULT_PORT;
    private String contextPath = "/";
    private String keystoreFile;
    private String keystorePassword;
    private String truststoreFile;
    private String truststorePassword;
    private long uploadMaxFileSize = -1L;
    private String uploadLocation = System.getProperty("java.io.tmpdir");
    private int uploadFileSizeThreshold = 1 * 1024 * 1024; // 1 MB

    public WebServerSettings(PippoSettings pippoSettings) {
        this.port = pippoSettings.getInteger(PippoConstants.SETTING_SERVER_PORT, DEFAULT_PORT);
        this.host = pippoSettings.getString(PippoConstants.SETTING_SERVER_HOST, host);
        this.contextPath = pippoSettings.getString(PippoConstants.SETTING_SERVER_CONTEXT_PATH, contextPath);
        this.keystoreFile = pippoSettings.getString(PippoConstants.SETTING_SERVER_KEYSTORE_FILE, keystoreFile);
        this.keystorePassword = pippoSettings.getString(PippoConstants.SETTING_SERVER_KEYSTORE_PASSWORD, keystorePassword);
        this.truststoreFile = pippoSettings.getString(PippoConstants.SETTING_SERVER_TRUSTSTORE_FILE, truststoreFile);
        this.truststorePassword = pippoSettings.getString(PippoConstants.SETTING_SERVER_TRUSTSTORE_PASSWORD, truststorePassword);
        this.uploadMaxFileSize = pippoSettings.getLong(PippoConstants.SETTING_UPLOAD_MAX_FILE_SIZE, uploadMaxFileSize);
        this.uploadLocation = pippoSettings.getString(PippoConstants.SETTING_UPLOAD_MAX_FILE_SIZE, uploadLocation);
        this.uploadFileSizeThreshold = pippoSettings.getInteger(PippoConstants.SETTING_UPLOAD_MAX_FILE_SIZE, uploadFileSizeThreshold);
    }

    public String getHost() {
        return host;
    }

    public WebServerSettings host(String host) {
        this.host = host;

        return this;
    }

    public int getPort() {
        return port;
    }

    public WebServerSettings port(int port) {
        this.port = port;

        return this;
    }

    public String getContextPath() {
        return contextPath;
    }

    public WebServerSettings contextPath(String contextPath) {
        this.contextPath = contextPath;

        return this;
    }

    public String getKeystoreFile() {
        return keystoreFile;
    }

    public WebServerSettings keystoreFile(String keystoreFile) {
        this.keystoreFile = keystoreFile;

        return this;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public WebServerSettings keystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;

        return this;
    }

    public String getTruststoreFile() {
        return truststoreFile;
    }

    public WebServerSettings truststoreFile(String truststoreFile) {
        this.truststoreFile = truststoreFile;

        return this;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public WebServerSettings truststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;

        return this;
    }

    /**
     * The maximum size allowed for uploaded files.
     */
    public long getUploadMaxFileSize() {
        return uploadMaxFileSize;
    }

    /**
     * The maximum size allowed for uploaded files.
     */
    public WebServerSettings uploadMaxFileSize(long uploadMaxFileSize) {
        this.uploadMaxFileSize = uploadMaxFileSize;

        return this;
    }

    /**
     * The directory location where files will be stored.
     */
    public String getUploadLocation() {
        return uploadLocation;
    }

    /**
     * The directory location where files will be stored.
     */
    public WebServerSettings uploadLocation(String uploadLocation) {
        this.uploadLocation = uploadLocation;

        return this;
    }

    /**
     * The size threshold after which files will be written to disk.
     */
    public int getUploadFileSizeThreshold() {
        return uploadFileSizeThreshold;
    }

    /**
     * The size threshold after which files will be written to disk.
     */
    public WebServerSettings uploadFileSizeThreshold(int uploadFileSizeThreshold) {
        this.uploadFileSizeThreshold = uploadFileSizeThreshold;

        return this;
    }

}
