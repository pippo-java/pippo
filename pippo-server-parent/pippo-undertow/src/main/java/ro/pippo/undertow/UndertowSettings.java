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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Option;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.WebServerSettings;
import ro.pippo.core.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class UndertowSettings extends WebServerSettings {

    private static final Logger log = LoggerFactory.getLogger(UndertowServer.class);

    public static final String BUFFER_SIZE = "undertow.bufferSize";
    private static final String PREFIX = "undertow.";
    private static final String UNDERTOW_SERVER_PREFIX = "undertow.server.";
    private static final String UNDERTOW_WORKER_PREFIX = "undertow.worker.";
    private static final String UNDERTOW_SOCKET_PREFIX = "undertow.socket.";
    public static final String BUFFERS_PER_REGION = "undertow.buffersPerRegion";
    public static final String DIRECT_BUFFERS = "undertow.directBuffers";
    public static final String IO_THREADS = "undertow.ioThreads";
    public static final String WORKER_THREADS = "undertow.workerThreads";

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

    /**
     *
     * @param builder - undertow builder
     * method adds undertow options to builder
     * undertow.server.* would be added to serverOptions
     * undertow.socket.* would be added to socketOptions
     * undertow.worker.* would be added to workerOptions
     */
    public void addUndertowOptions(Undertow.Builder builder) {
        List<String> propertyNames = pippoSettings.getSettingNames(PREFIX);
        String prefix;
        for (String propertyName: propertyNames) {
            prefix = null;
            if (propertyName.startsWith(UNDERTOW_SERVER_PREFIX)) {
                prefix = UNDERTOW_SERVER_PREFIX;
            } else if (propertyName.startsWith(UNDERTOW_SOCKET_PREFIX)) {
                prefix = UNDERTOW_SOCKET_PREFIX;
            } else if (propertyName.startsWith(UNDERTOW_WORKER_PREFIX)) {
                prefix = UNDERTOW_WORKER_PREFIX;
            }
            if (prefix != null) {
                addUndertowOption(builder, propertyName, prefix);
            }
        }
    }

    /**
     *
     * @return - returns Option type as String
     * Currently, only Options in UndertowOptions are supported
     */
    private String getTypeName(String parameter) {
        try {
            Object value = getOption(parameter);
            if (value != null) {
                Field optionField = value.getClass().getDeclaredField("type");
                optionField.setAccessible(true);
                return optionField.get(value).toString();
            }
        } catch (Exception e) {
            log.debug("getting Option type for parameter {} failed with {}", parameter, e);
        }
        return null;
    }


    /**
     * Returns Options for given parameter if present else null
     */
    private <T> Option<T> getOption(String parameter) {
        try {
            Field field = UndertowOptions.class.getDeclaredField(parameter);
            if (Option.class.getName().equals(field.getType().getTypeName())) {
                Object value = field.get(null);
                return (Option<T>) value;
            }
        } catch (Exception e) {
            log.debug("getting Option type for parameter {} failed with {}", parameter, e);
        }
        return null;
    }

    /**
     *
     * Add UndertowOption to builder based on the prefix
     * Currently supported Option types - Integer, String, Long, and Boolean
     * @param builder - undertow builder
     * @param propertyName - name of property to be set in undertow
     * @param prefix - defines property type (SERVER, SOCKET, WORKER)
     */
    private void addUndertowOption(Undertow.Builder builder, String propertyName, String prefix) {
        String undertowPropName = StringUtils.removeStart(propertyName, prefix);
        String typeName = getTypeName(undertowPropName);
        if (StringUtils.isNullOrEmpty(typeName))
            return;

        if (typeName.equals(Integer.class.toString())) {
            int value = pippoSettings.getInteger(propertyName, Integer.MIN_VALUE);
            if (value > Integer.MIN_VALUE) {
                Option<Integer> option = getOption(undertowPropName);
                addUndertowOption(builder, option, value, prefix);
            }
        } else if (typeName.equals(Long.class.toString())) {
            long value = pippoSettings.getLong(propertyName, Long.MIN_VALUE);
            if (value > Long.MIN_VALUE) {
                Option<Long> option = getOption(undertowPropName);
                addUndertowOption(builder, option, value, prefix);
            }
        } else if (typeName.equals(String.class.toString())) {
            Option<String> option = getOption(undertowPropName);
            addUndertowOption(builder, option, pippoSettings.getString(propertyName, ""), prefix);
        } else if (typeName.equals(Boolean.class.toString())) {
            Option<Boolean> option = getOption(undertowPropName);
            addUndertowOption(builder, option, pippoSettings.getBoolean(propertyName, false), prefix);
        }
    }

    private <T> void addUndertowOption(Undertow.Builder builder, Option<T> option, T value, String prefix) {
        switch (prefix) {
            case UNDERTOW_SERVER_PREFIX:
                builder.setServerOption(option, value);
                break;
            case UNDERTOW_SOCKET_PREFIX:
                builder.setSocketOption(option, value);
                break;
            case UNDERTOW_WORKER_PREFIX:
                builder.setWorkerOption(option, value);
                break;
            default:
                break;
        }
    }

}
