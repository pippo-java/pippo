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
