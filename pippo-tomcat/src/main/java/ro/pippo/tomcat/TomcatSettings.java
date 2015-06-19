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
package ro.pippo.tomcat;

import java.util.HashMap;
import java.util.Map;

import ro.pippo.core.PippoSettings;
import ro.pippo.core.WebServerSettings;

/**
 * @author Decebal Suiu, Gustavo Galvan
 */
public class TomcatSettings extends WebServerSettings {

	// http://tomcat.apache.org/tomcat-8.0-doc/config/http.html
	
	private Map<String,Object> settings;

    public TomcatSettings(PippoSettings pippoSettings) {
        super(pippoSettings);
        
        settings = new HashMap<String,Object>();
        
        // Common attributes
        settings.put("allowTrace", (pippoSettings.hasSetting("tomcat.allowTrace")) ? pippoSettings.getBoolean("tomcat.allowTrace", false) : null);
        settings.put("asyncTimeout", (pippoSettings.hasSetting("tomcat.asyncTimeout")) ? pippoSettings.getInteger("tomcat.asyncTimeout", 30000) : null);
        settings.put("enableLookups", (pippoSettings.hasSetting("tomcat.enableLookups")) ? pippoSettings.getBoolean("tomcat.enableLookups", false) : null);
        settings.put("maxHeaderCount", (pippoSettings.hasSetting("tomcat.maxHeaderCount")) ? pippoSettings.getInteger("tomcat.maxHeaderCount", 100) : null);
        settings.put("maxParameterCount", (pippoSettings.hasSetting("tomcat.maxParameterCount")) ? pippoSettings.getInteger("tomcat.maxParameterCount", 10000) : null);
        settings.put("maxPostSize", (pippoSettings.hasSetting("tomcat.maxPostSize")) ? pippoSettings.getInteger("tomcat.maxPostSize", 2097152 ) : null);
        settings.put("maxSavePostSize", (pippoSettings.hasSetting("tomcat.maxSavePostSize")) ? pippoSettings.getInteger("tomcat.maxSavePostSize", 4096) : null);
        settings.put("parseBodyMethods", (pippoSettings.hasSetting("tomcat.parseBodyMethods")) ? pippoSettings.getString("tomcat.parseBodyMethods", "POST") : null);
        settings.put("protocol", (pippoSettings.hasSetting("tomcat.protocol")) ? pippoSettings.getString("tomcat.protocol", "HTTP/1.1") : null);
        settings.put("proxyName", (pippoSettings.hasSetting("tomcat.proxyName")) ? pippoSettings.getString("tomcat.proxyName", "") : null);
        settings.put("proxyPort", (pippoSettings.hasSetting("tomcat.proxyPort")) ? pippoSettings.getInteger("tomcat.proxyPort", 0) : null);
        settings.put("redirectPort", (pippoSettings.hasSetting("tomcat.redirectPort")) ? pippoSettings.getInteger("tomcat.redirectPort", 0) : null);
        settings.put("scheme", (pippoSettings.hasSetting("tomcat.scheme")) ? pippoSettings.getString("tomcat.scheme", "http") : null);
        settings.put("secure", (pippoSettings.hasSetting("tomcat.secure")) ? pippoSettings.getBoolean("tomcat.secure", false) : null);
        settings.put("URIEncoding", (pippoSettings.hasSetting("tomcat.URIEncoding")) ? pippoSettings.getString("tomcat.URIEncoding", "UTF-8") : null);
        settings.put("useBodyEncodingForURI", (pippoSettings.hasSetting("tomcat.useBodyEncodingForURI")) ? pippoSettings.getBoolean("tomcat.useBodyEncodingForURI", false) : null);
        settings.put("useIPVHosts", (pippoSettings.hasSetting("tomcat.useIPVHosts")) ? pippoSettings.getBoolean("tomcat.", false) : null);
        settings.put("xpoweredBy", (pippoSettings.hasSetting("tomcat.xpoweredBy")) ? pippoSettings.getBoolean("tomcat.xpoweredBy", false) : null);
        
        // Standard implementation
        settings.put("acceptCount", (pippoSettings.hasSetting("tomcat.acceptCount")) ? pippoSettings.getInteger("tomcat.acceptCount", 100) : null);
        settings.put("acceptorThreadCount", (pippoSettings.hasSetting("tomcat.acceptorThreadCount")) ? pippoSettings.getInteger("tomcat.acceptorThreadCount", 1) : null);
        settings.put("acceptorThreadPriority", (pippoSettings.hasSetting("tomcat.acceptorThreadPriority")) ? pippoSettings.getInteger("tomcat.acceptorThreadPriority", 5) : null);
        settings.put("allowTrailerHeaders", (pippoSettings.hasSetting("tomcat.allowTrailerHeaders")) ? pippoSettings.getString("tomcat.allowTrailerHeaders", "") : null);
        settings.put("BindOnInit", (pippoSettings.hasSetting("tomcat.BindOnInit")) ? pippoSettings.getBoolean("tomcat.BindOnInit", true) : null);
        settings.put("compressableMimeType", (pippoSettings.hasSetting("tomcat.compressableMimeType")) ? pippoSettings.getString("tomcat.compressableMimeType", "text/html,text/xml,text/plain,text/css") : null);
        settings.put("compression", (pippoSettings.hasSetting("tomcat.compression")) ? pippoSettings.getString("tomcat.compression", "off") : null);
        settings.put("compressionMinSize", (pippoSettings.hasSetting("tomcat.compressionMinSize")) ? pippoSettings.getInteger("tomcat.compressionMinSize", 2048) : null);
        settings.put("connectionLinger", (pippoSettings.hasSetting("tomcat.connectionLinger")) ? pippoSettings.getInteger("tomcat.connectionLinger", -1) : null);
        settings.put("connectionTimeout", (pippoSettings.hasSetting("tomcat.connectionTimeout")) ? pippoSettings.getInteger("tomcat.connectionTimeout", 60000) : null);
        settings.put("connectionUploadTimeout", (pippoSettings.hasSetting("tomcat.connectionUploadTimeout")) ? pippoSettings.getInteger("tomcat.connectionUploadTimeout", 600000) : null);
        settings.put("disableUploadTimeout", (pippoSettings.hasSetting("tomcat.disableUploadTimeout")) ? pippoSettings.getBoolean("tomcat.disableUploadTimeout", true) : null);
        settings.put("keepAliveTimeout", (pippoSettings.hasSetting("tomcat.keepAliveTimeout")) ? pippoSettings.getInteger("tomcat.keepAliveTimeout", 60000) : null);
        settings.put("maxConnections", (pippoSettings.hasSetting("tomcat.maxConnections")) ? pippoSettings.getInteger("tomcat.maxConnections", 10000) : null); // NIO=10000; BIO=maxThreads; APR=8192
        settings.put("maxExtensionSize", (pippoSettings.hasSetting("tomcat.maxExtensionSize")) ? pippoSettings.getInteger("tomcat.maxExtensionSize", 8192) : null);
        settings.put("maxHttpHeaderSize", (pippoSettings.hasSetting("tomcat.maxHttpHeaderSize")) ? pippoSettings.getInteger("tomcat.maxHttpHeaderSize", 8192) : null);
        settings.put("maxKeepAliveRequests", (pippoSettings.hasSetting("tomcat.maxKeepAliveRequests")) ? pippoSettings.getInteger("tomcat.maxKeepAliveRequests", 100) : null);
        settings.put("maxShallowSize", (pippoSettings.hasSetting("tomcat.maxShallowSize")) ? pippoSettings.getInteger("tomcat.maxShallowSize", 2097152) : null);
        settings.put("maxThreads", (pippoSettings.hasSetting("tomcat.maxThreads")) ? pippoSettings.getInteger("tomcat.maxThreads", 200) : null);
        settings.put("maxTrailerSize", (pippoSettings.hasSetting("tomcat.maxTrailerSize")) ? pippoSettings.getInteger("tomcat.maxTrailerSize", 8192) : null);
        settings.put("minSpareThreads", (pippoSettings.hasSetting("tomcat.minSpareThreads")) ? pippoSettings.getInteger("tomcat.minSpareThreads", 10) : null);
        settings.put("noCompressionUserAgents", (pippoSettings.hasSetting("tomcat.noCompressionUserAgents")) ? pippoSettings.getString("tomcat.noCompressionUserAgents", "") : null);
        settings.put("processorCache", (pippoSettings.hasSetting("tomcat.processorCache")) ? pippoSettings.getInteger("tomcat.processorCache", 200) : null);
        settings.put("restrictedUserAgents", (pippoSettings.hasSetting("tomcat.restrictedUserAgents")) ? pippoSettings.getString("tomcat.restrictedUserAgents", "") : null);
        settings.put("server", (pippoSettings.hasSetting("tomcat.server")) ? pippoSettings.getString("tomcat.server", "Apache-Coyote/1.1") : null);
        settings.put("socketBuffer", (pippoSettings.hasSetting("tomcat.socketBuffer")) ? pippoSettings.getInteger("tomcat.socketBuffer", 9000) : null);
        settings.put("tcpNoDelay", (pippoSettings.hasSetting("tomcat.tcpNoDelay")) ? pippoSettings.getBoolean("tomcat.tcpNoDelay", true) : null);
        settings.put("threadPriority", (pippoSettings.hasSetting("tomcat.threadPriority")) ? pippoSettings.getInteger("tomcat.threadPriority", 5) : null);
        settings.put("upgradeAsyncWriteBufferSize", (pippoSettings.hasSetting("tomcat.upgradeAsyncWriteBufferSize")) ? pippoSettings.getInteger("tomcat.upgradeAsyncWriteBufferSize", 8192) : null);
        
        // Java TCP socket attributes ( BIO, NIO and NIO2)
        settings.put("socket.rxBufSize", (pippoSettings.hasSetting("tomcat.socket.rxBufSize")) ? pippoSettings.getInteger("tomcat.socket.rxBufSize", 8192) : null);
        settings.put("socket.txBufSize", (pippoSettings.hasSetting("tomcat.socket.txBufSize")) ? pippoSettings.getInteger("tomcat.socket.txBufSize", 8192) : null);
        settings.put("socket.tcpNoDelay", (pippoSettings.hasSetting("tomcat.socket.tcpNoDelay")) ? pippoSettings.getBoolean("tomcat.socket.tcpNoDelay", true) : null);
        settings.put("socket.soKeepAlive", (pippoSettings.hasSetting("tomcat.socket.soKeepAlive")) ? pippoSettings.getBoolean("tomcat.socket.soKeepAlive", true) : null);
        settings.put("socket.ooBInline", (pippoSettings.hasSetting("tomcat.socket.ooBInline")) ? pippoSettings.getBoolean("tomcat.socket.ooBInline", false) : null);
        settings.put("socket.soReuseAddress", (pippoSettings.hasSetting("tomcat.socket.soReuseAddress")) ? pippoSettings.getBoolean("tomcat.socket.soReuseAddress", true) : null);
        settings.put("socket.soLingerOn", (pippoSettings.hasSetting("tomcat.socket.soLingerOn")) ? pippoSettings.getBoolean("tomcat.socket.soLingerOn", false) : null);
        settings.put("socket.soLingerTime", (pippoSettings.hasSetting("tomcat.socket.soLingerTime")) ? pippoSettings.getInteger("tomcat.socket.soLingerTime", 1) : null);
        settings.put("socket.soTimeout", (pippoSettings.hasSetting("tomcat.socket.soTimeout")) ? pippoSettings.getInteger("tomcat.socket.soTimeout", 60000) : null);
        // performance balance: all three must be configured (eg: 0 1 0) or none
        settings.put("socket.performanceConnectionTime", (pippoSettings.hasSetting("tomcat.socket.performanceConnectionTime")) ? pippoSettings.getInteger("tomcat.socket.performanceConnectionTime", 2) : null);
        settings.put("socket.performanceLatency", (pippoSettings.hasSetting("tomcat.socket.performanceLatency")) ? pippoSettings.getInteger("tomcat.socket.performanceLatency", 1) : null);
        settings.put("socket.performanceBandwidth", (pippoSettings.hasSetting("tomcat.socket.performanceBandwidth")) ? pippoSettings.getInteger("tomcat.socket.performanceBandwidth", 0) : null);

        // NIO specific configuration
        settings.put("pollerThreadCount", (pippoSettings.hasSetting("tomcat.pollerThreadCount")) ? pippoSettings.getInteger("tomcat.pollerThreadCount", 2) : null);
        settings.put("pollerThreadPriority", (pippoSettings.hasSetting("tomcat.pollerThreadPriority")) ? pippoSettings.getInteger("tomcat.pollerThreadPriority", 5) : null);
        settings.put("selectorTimeout", (pippoSettings.hasSetting("tomcat.selectorTimeout")) ? pippoSettings.getInteger("tomcat.selectorTimeout", 1000) : null);
        settings.put("useComet", (pippoSettings.hasSetting("tomcat.useComet")) ? pippoSettings.getBoolean("tomcat.useComet", true) : null);
        settings.put("useSendfile", (pippoSettings.hasSetting("tomcat.useSendfile")) ? pippoSettings.getBoolean("tomcat.useSendfile", true) : null);
        settings.put("socket.directBuffer", (pippoSettings.hasSetting("tomcat.socket.directBuffer")) ? pippoSettings.getBoolean("tomcat.socket.directBuffer", false) : null);
        settings.put("socket.appReadBufSize", (pippoSettings.hasSetting("tomcat.socket.appReadBufSize")) ? pippoSettings.getInteger("tomcat.socket.appReadBufSize", 8192) : null);
        settings.put("socket.appWriteBufSize", (pippoSettings.hasSetting("tomcat.socket.appWriteBufSize")) ? pippoSettings.getInteger("tomcat.socket.appWriteBufSize", 8192) : null);
        settings.put("socket.bufferPool", (pippoSettings.hasSetting("tomcat.socket.bufferPool")) ? pippoSettings.getInteger("tomcat.socket.bufferPool", 500) : null);
        settings.put("socket.bufferPoolSize", (pippoSettings.hasSetting("tomcat.socket.bufferPoolSize")) ? pippoSettings.getInteger("tomcat.socket.bufferPoolSize", 104857600) : null);
        settings.put("socket.processorCache", (pippoSettings.hasSetting("tomcat.socket.processorCache")) ? pippoSettings.getInteger("tomcat.socket.processorCache", 500) : null);
        settings.put("socket.keyCache", (pippoSettings.hasSetting("tomcat.socket.keyCache")) ? pippoSettings.getInteger("tomcat.socket.keyCache", 500) : null);
        settings.put("socket.eventCache", (pippoSettings.hasSetting("tomcat.socket.eventCache")) ? pippoSettings.getInteger("tomcat.socket.eventCache", 500) : null);
        settings.put("selectorPool.maxSelectors", (pippoSettings.hasSetting("tomcat.selectorPool.maxSelectors")) ? pippoSettings.getInteger("tomcat.selectorPool.maxSelectors", 200) : null);
        settings.put("selectorPool.maxSpareSelectors", (pippoSettings.hasSetting("tomcat.selectorPool.maxSpareSelectors")) ? pippoSettings.getInteger("tomcat.selectorPool.maxSpareSelectors", -1) : null);
        settings.put("oomParachute", (pippoSettings.hasSetting("tomcat.oomParachute")) ? pippoSettings.getInteger("tomcat.oomParachute",  1048576) : null);
        
    }

    /**
     * Get all settings
     * @return HashMap
     */
    public Map<String,Object> getSettingsMap() {
    	return settings;
    }
    
    /**
     * Get setting
     * @return
     */
    public Object getSetting(String key) {
    	return settings.get(key);
    }
    
    /**
     * Set setting
     * @return
     */
    public TomcatSettings setSetting(String key, int value) {
    	settings.put(key, value);
    	return this;
    }
    
    public TomcatSettings setSetting(String key, long value) {
    	settings.put(key, value);
    	return this;
    }

    public TomcatSettings setSetting(String key, String value) {
    	settings.put(key, value);
    	return this;
    }
    
    public TomcatSettings setSetting(String key, Boolean value) {
    	settings.put(key, value);
    	return this;
    }

}
