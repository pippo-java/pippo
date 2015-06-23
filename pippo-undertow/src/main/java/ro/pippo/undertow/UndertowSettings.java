/*
 * Copyright (C) 2015 the original author or authors.
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
package ro.pippo.undertow;

import java.util.HashMap;
import java.util.Map;

import ro.pippo.core.PippoSettings;
import ro.pippo.core.WebServerSettings;

/**
 * @author Decebal Suiu
 */
public class UndertowSettings extends WebServerSettings {

    public static final String BUFFER_SIZE = "undertow.bufferSize";
    public static final String BUFFERS_PER_REGION = "undertow.buffersPerRegion";
    public static final String DIRECT_BUFFERS = "undertow.directBuffers";
    public static final String IO_THREADS = "undertow.ioThreads";
    public static final String WORKER_THREADS = "undertow.workerThreads";

    private int bufferSize;
    private int buffersPerRegion;
    private int ioThreads;
    private int workerThreads;
    private Boolean directBuffers;
    
    private Map<String,Object> workerOptions;
    private Map<String,Object> socketOptions;
    private Map<String,Object> serverOptions;
    
    public UndertowSettings(PippoSettings pippoSettings) {
        super(pippoSettings);

        bufferSize = pippoSettings.getInteger(UndertowSettings.BUFFER_SIZE, 0);
        buffersPerRegion = pippoSettings.getInteger(UndertowSettings.BUFFERS_PER_REGION, 0);
        directBuffers = pippoSettings.getBoolean(UndertowSettings.DIRECT_BUFFERS, false);
        ioThreads = pippoSettings.getInteger(UndertowSettings.IO_THREADS, 0);
        workerThreads = pippoSettings.getInteger(UndertowSettings.WORKER_THREADS, 0);
        
        workerOptions = new HashMap<String,Object>();
        socketOptions = new HashMap<String,Object>();
        serverOptions = new HashMap<String,Object>();

        // builder.setWorkerOptions
        workerOptions.put("tcpNoDelay", (pippoSettings.hasSetting("undertow.tcpNoDelay")) ? pippoSettings.getBoolean("undertow.tcpNoDelay", false) : null);
        
        // builder.setSocketOptions
        socketOptions.put("tcpNoDelay", (pippoSettings.hasSetting("undertow.tcpNoDelay")) ? pippoSettings.getBoolean("undertow.tcpNoDelay", false) : null);
        socketOptions.put("reuseAddresses", (pippoSettings.hasSetting("undertow.reuseAddresses")) ? pippoSettings.getBoolean("undertow.reuseAddresses", false) : null);
        
        // builder.setServerOptions
        serverOptions.put("maxHeaderSize", (pippoSettings.hasSetting("undertow.maxHeaderSize")) ? pippoSettings.getInteger("undertow.maxHeaderSize", 51200) : null);
        serverOptions.put("maxEntitySize", (pippoSettings.hasSetting("undertow.maxEntitySize")) ? pippoSettings.getInteger("undertow.maxEntitySize", 1048576) : null);
        serverOptions.put("maxParameters", (pippoSettings.hasSetting("undertow.maxParameters")) ? pippoSettings.getInteger("undertow.maxParameters", 1000) : null);
        serverOptions.put("maxHeaders", (pippoSettings.hasSetting("undertow.maxHeaders")) ? pippoSettings.getInteger("undertow.maxHeaders", 200) : null);
        serverOptions.put("maxCookies", (pippoSettings.hasSetting("undertow.maxCookies")) ? pippoSettings.getInteger("undertow.maxCookies", 200) : null);
        serverOptions.put("urlCharset", (pippoSettings.hasSetting("undertow.urlCharset")) ? pippoSettings.getString("undertow.urlCharset", "UTF-8") : null);
        serverOptions.put("decodeUrl", (pippoSettings.hasSetting("undertow.decodeUrl")) ? pippoSettings.getBoolean("undertow.decodeUrl", true) : null);
        serverOptions.put("AllowEncodedSlash", (pippoSettings.hasSetting("undertow.AllowEncodedSlash")) ? pippoSettings.getBoolean("undertow.AllowEncodedSlash", false) : null);
        serverOptions.put("AllowEqualsInCookieValue", (pippoSettings.hasSetting("undertow.AllowEqualsInCookieValue")) ? pippoSettings.getBoolean("undertow.AllowEqualsInCookieValue", false) : null);
        serverOptions.put("AlwaysSetDate", (pippoSettings.hasSetting("undertow.AlwaysSetDate")) ? pippoSettings.getBoolean("undertow.AlwaysSetDate", true) : null);
        serverOptions.put("AlwaysSetKeepAlive", (pippoSettings.hasSetting("undertow.AlwaysSetKeepAlive")) ? pippoSettings.getBoolean("undertow.AlwaysSetKeepAlive", true) : null);
        serverOptions.put("MaxBufferedRequestSize", (pippoSettings.hasSetting("undertow.MaxBufferedRequestSize")) ? pippoSettings.getInteger("undertow.MaxBufferedRequestSize", 16384) : null);
        serverOptions.put("RecordRequestStartTime", (pippoSettings.hasSetting("undertow.RecordRequestStartTime")) ? pippoSettings.getBoolean("undertow.RecordRequestStartTime", false) : null);
        serverOptions.put("IdleTimeout", (pippoSettings.hasSetting("undertow.IdleTimeout")) ? pippoSettings.getInteger("undertow.IdleTimeout", 60000) : null);
        serverOptions.put("RequestParseTimeout", (pippoSettings.hasSetting("undertow.RequestParseTimeout")) ? pippoSettings.getInteger("undertow.RequestParseTimeout", -1) : null);
        serverOptions.put("EnableConnectorStatistics", (pippoSettings.hasSetting("undertow.EnableConnectorStatistics")) ? pippoSettings.getBoolean("undertow.EnableConnectorStatistics", false) : null);
        // TODO: SSL options
        // TODO: SPDY options
        // TODO: HTTP2 options
    }

    public Map<String,Object> getWorkerOptions() {
    	return workerOptions;
    }
    
    public Map<String,Object> getSocketOptions() {
    	return socketOptions;
    }
    
    public Map<String,Object> getServerOptions() {
    	return serverOptions;
    }
    
    public UndertowSettings setWorkerOption(String key, Object value) {
    	workerOptions.put(key, value);
    	return this;
    }
    
    public UndertowSettings setSocketOption(String key, Object value) {
    	socketOptions.put(key, value);
    	return this;
    }

    public UndertowSettings setServerOption(String key, Object value) {
    	serverOptions.put(key, value);
    	return this;
    }
    
    public int getBufferSize() {
        return bufferSize;
    }

    public int getBuffersPerRegion() {
        return buffersPerRegion;
    }

    public boolean getDirectBuffers() {
        return directBuffers;
    }

    public int getIoThreads() {
        return ioThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public UndertowSettings setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public UndertowSettings setBuffersPerRegion(int buffersPerRegion) {
        this.buffersPerRegion = buffersPerRegion;
        return this;
    }

    public UndertowSettings setDirectBuffers(boolean directBuffers) {
        this.directBuffers = directBuffers;
        return this;
    }

    public UndertowSettings setIoThreads(int ioThreads) {
        this.ioThreads = ioThreads;
        return this;
    }

    public UndertowSettings setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
        return this;
    }

}
