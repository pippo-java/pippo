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

    public UndertowSettings(PippoSettings pippoSettings) {
        super(pippoSettings);

        bufferSize = pippoSettings.getInteger(UndertowSettings.BUFFER_SIZE, 0);
        buffersPerRegion = pippoSettings.getInteger(UndertowSettings.BUFFERS_PER_REGION, 0);
        directBuffers = pippoSettings.getBoolean(UndertowSettings.DIRECT_BUFFERS, false);
        ioThreads = pippoSettings.getInteger(UndertowSettings.IO_THREADS, 0);
        workerThreads = pippoSettings.getInteger(UndertowSettings.WORKER_THREADS, 0);
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
