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

import ro.pippo.core.PippoSettings;
import ro.pippo.core.WebServerSettings;

/**
 * @author Daniel Jipa
 */
public class TomcatSettings extends WebServerSettings {

    public static final String BASE_FOLDER = "tomcat.baseFolder";
    public static final String MAX_CONNECTIONS = "tomcat.maxConnections";
    public static final String KEY_ALIAS = "tomcat.keyAlias";
    public static final String KEY_TYPE = "tomcat.keyType";
    public static final String CLIENT_AUTH = "tomcat.clientAuth";

    private String baseFolder;
    private int maxConnections;
    private String keyAlias;
    private String keyType;
    private boolean clientAuth;

    public TomcatSettings(PippoSettings pippoSettings) {
        super(pippoSettings);

        this.baseFolder = pippoSettings.getString(TomcatSettings.BASE_FOLDER, "tomcat_home");
        this.maxConnections = pippoSettings.getInteger(TomcatSettings.MAX_CONNECTIONS, 100);
        this.keyAlias = pippoSettings.getString(TomcatSettings.KEY_ALIAS, "tomcat");
        this.keyType = pippoSettings.getString(TomcatSettings.KEY_TYPE, "JKS");
        this.clientAuth = pippoSettings.getBoolean(TomcatSettings.CLIENT_AUTH, false);
    }

    public String getBaseFolder() {
        return baseFolder;
    }

    public int getMaxConnections(){
        return maxConnections;
    }

    public String getKeyType() {
        return keyType;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public boolean getClientAuth() {
        return clientAuth;
    }

    public TomcatSettings baseFolder(String baseFolder) {
        this.baseFolder = baseFolder;

        return this;
    }

    public TomcatSettings maxConnections(int maxConnections) {
        this.maxConnections = maxConnections;

        return this;
    }

    public TomcatSettings keyAlias(String keyAlias) {
        this.keyAlias = keyAlias;

        return this;
    }

    public TomcatSettings keyType(String keyType) {
        this.keyType = keyType;

        return this;
    }

    public TomcatSettings setClientAuth(boolean clientAuth) {
        this.clientAuth = clientAuth;

        return this;
    }

}
