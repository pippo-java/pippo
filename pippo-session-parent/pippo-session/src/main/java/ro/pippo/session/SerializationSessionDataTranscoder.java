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
package ro.pippo.session;

import ro.pippo.core.PippoRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

/**
 * A {@link SessionDataTranscoder} that serializes {@link SessionData}s using
 * java serialization.
 *
 * @author Decebal Suiu
 */
public class SerializationSessionDataTranscoder implements SessionDataTranscoder {

    @Override
    public String encode(SessionData sessionData) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(sessionData);
            byte[] bytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public SessionData decode(String data) {
        byte[] bytes = Base64.getDecoder().decode(data);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             FilteringObjectInputStream objectInputStream = new FilteringObjectInputStream(inputStream)) {
            return (SessionData) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new PippoRuntimeException(e, "Cannot deserialize session. A new one will be created.");
        }
    }

}
