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

import java.io.Serializable;

/**
 * @author Decebal Suiu
 */
public class WebServerSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int defaultPort = 8338;

    private String host = "localhost";
    private int port = defaultPort;
    private String contextPath = "/";
    private String keystoreFile;
    private String keystorePassword;
    private String truststoreFile;
    private String truststorePassword;

    private int maxThreads = 100;
    private int minThreads = 10;
    private int threadTimeout = 30000;

    public WebServerSettings(PippoSettings pippoSettings) {
        this.port = pippoSettings.getInteger(PippoConstants.SETTING_SERVER_PORT, defaultPort);
        this.host = pippoSettings.getString(PippoConstants.SETTING_SERVER_HOST, host);
        this.contextPath = pippoSettings.getString(PippoConstants.SETTING_SERVER_CONTEXT_PATH, contextPath);
        this.keystoreFile = pippoSettings.getString(PippoConstants.SETTING_SERVER_KEYSTORE_FILE, keystoreFile);
        this.keystorePassword = pippoSettings.getString(PippoConstants.SETTING_SERVER_KEYSTORE_PASSWORD, keystorePassword);
        this.truststoreFile = pippoSettings.getString(PippoConstants.SETTING_SERVER_TRUSTSTORE_FILE, truststoreFile);
        this.truststorePassword = pippoSettings.getString(PippoConstants.SETTING_SERVER_TRUSTSTORE_PASSWORD, truststorePassword);

        this.maxThreads = pippoSettings.getInteger(PippoConstants.SETTING_SERVER_MAX_THREADS, maxThreads);
        this.minThreads = pippoSettings.getInteger(PippoConstants.SETTING_SERVER_MIN_THREADS, minThreads);
        this.threadTimeout = pippoSettings.getInteger(PippoConstants.SETTING_SERVER_THREAD_TIMEOUT, threadTimeout);
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

    public int getMaxThreads() {
        return maxThreads;
    }

    public WebServerSettings maxThreads(int maxThreads) {
        this.maxThreads = maxThreads;

        return this;
    }

    public int getMinThreads() {
        return minThreads;
    }

    public WebServerSettings minThreads(int minThreads) {
        this.minThreads = minThreads;

        return this;
    }

    public int getThreadTimeout() {
        return threadTimeout;
    }

    public WebServerSettings threadTimeout(int threadTimeout) {
        this.threadTimeout = threadTimeout;

        return this;
    }

}
