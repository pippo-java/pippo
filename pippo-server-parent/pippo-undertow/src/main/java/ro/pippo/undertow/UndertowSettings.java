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

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import org.xnio.Option;
import org.xnio.OptionMap;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.WebServerSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class UndertowSettings extends WebServerSettings {

    public static final String BUFFER_SIZE = "undertow.bufferSize";
    public static final String BUFFERS_PER_REGION = "undertow.buffersPerRegion";
    public static final String DIRECT_BUFFERS = "undertow.directBuffers";
    public static final String IO_THREADS = "undertow.ioThreads";
    public static final String WORKER_THREADS = "undertow.workerThreads";
    private static Map<String, Class> parameterOptionMap;

    private interface UndertowParameters {
        String SERVER_PREFIX = "undertow.server.";
        String WORKER_PREFIX = "undertow.worker.";
        String SOCKET_PREFIX = "undertow.socket.";

        // add all undertow parameters
        String MAX_PARAMETERS = "undertow.server.MAX_PARAMETERS";
        String IDLE_TIMEOUT = "undertow.server.IDLE_TIMEOUT";

    }

    private int bufferSize;
    private int buffersPerRegion;
    private int ioThreads;
    private int workerThreads;
    private Boolean directBuffers;
    private PippoSettings pippoSettings;

    public UndertowSettings(PippoSettings pippoSettings) {
        super(pippoSettings);

        bufferSize = pippoSettings.getInteger(UndertowSettings.BUFFER_SIZE, 0);
        buffersPerRegion = pippoSettings.getInteger(UndertowSettings.BUFFERS_PER_REGION, 0);
        directBuffers = pippoSettings.getBoolean(UndertowSettings.DIRECT_BUFFERS, false);
        ioThreads = pippoSettings.getInteger(UndertowSettings.IO_THREADS, 0);
        workerThreads = pippoSettings.getInteger(UndertowSettings.WORKER_THREADS, 0);

        this.pippoSettings = pippoSettings;
        initializeOptionsMap();
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

    private void initializeOptionsMap() {
        if (parameterOptionMap == null) {
            parameterOptionMap = new HashMap<>();
            parameterOptionMap.put(UndertowParameters.MAX_PARAMETERS, Integer.class);
            parameterOptionMap.put(UndertowParameters.IDLE_TIMEOUT, Integer.class);
        }
    }

    public void addUndertowOptions(Undertow.Builder builder) {
        List<String> propertyNames = pippoSettings.getSettingNames();
        for (String propertyName: propertyNames) {
            if (parameterOptionMap.containsKey(propertyName)) {
                if (propertyName.startsWith(UndertowParameters.SERVER_PREFIX)) {
                    addUndertowOption(builder, propertyName, UndertowParameters.SERVER_PREFIX);
                } else if (propertyName.startsWith(UndertowParameters.SOCKET_PREFIX)) {
                    addUndertowOption(builder, propertyName, UndertowParameters.SOCKET_PREFIX);
                } else if (propertyName.startsWith(UndertowParameters.WORKER_PREFIX)) {
                    addUndertowOption(builder, propertyName, UndertowParameters.WORKER_PREFIX);
                }
            }
        }
    }

    private void addUndertowOption(Undertow.Builder builder, String propertyName, String prefix) {
        Class propertyType = parameterOptionMap.get(propertyName);
        if (propertyType != null) {
            if (propertyType.equals(Integer.class)) {
                int value = pippoSettings.getInteger(propertyName, -1);
                if (value != -1) {
                    Option<Integer> option = Option.simple(
                            UndertowOptions.class, propertyName.replace(prefix, ""), Integer.class);
                    addUndertowOption(builder, option, value, prefix);
                }
            } else if (propertyType.equals(Long.class)) {
                long value = pippoSettings.getLong(propertyName, -1L);
                if (value != -1) {
                    Option<Long> option = Option.simple(
                            UndertowOptions.class, propertyName.replace(prefix, ""), Long.class);
                    addUndertowOption(builder, option, value, prefix);
                }
            } else if (propertyType.equals(String.class)) {
                Option<String> option = Option.simple(
                        UndertowOptions.class, propertyName.replace(prefix, ""), String.class);
                addUndertowOption(builder, option, pippoSettings.getString(propertyName, ""), prefix);
            } else if (propertyType.equals(Boolean.class)) {
                Option<Boolean> option = Option.simple(
                        UndertowOptions.class, propertyName.replace(prefix, ""), Boolean.class);
                addUndertowOption(builder, option, pippoSettings.getBoolean(propertyName, false), prefix);
            }
        }
    }

    private <T> void addUndertowOption(Undertow.Builder builder, Option<T> option, T value, String prefix) {
        switch (prefix) {
            case UndertowParameters.SERVER_PREFIX:
                builder.setServerOption(option, value);
                break;
            case UndertowParameters.SOCKET_PREFIX:
                builder.setServerOption(option, value);
                break;
            case UndertowParameters.WORKER_PREFIX:
                builder.setWorkerOption(option, value);
                break;
            default:
                break;
        }
    }

}
