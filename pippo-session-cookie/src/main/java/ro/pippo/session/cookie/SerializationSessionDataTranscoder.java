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
package ro.pippo.session.cookie;

import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.IoUtils;
import ro.pippo.session.SessionData;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A {@link SessionDataTranscoder} that serializes {@link SessionData}s using java serialization.
 *
 * @author Decebal Suiu
 */
public class SerializationSessionDataTranscoder implements SessionDataTranscoder {

    @Override
    public String encode(SessionData sessionData) {
        ByteArrayOutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(sessionData);
            byte[] bytes = outputStream.toByteArray();

            // TODO use java.util.Base64 from java 8
            return DatatypeConverter.printBase64Binary(bytes);
        }catch (IOException e) {
            throw new PippoRuntimeException(e);
        } finally {
            IoUtils.close(objectOutputStream);
            IoUtils.close(outputStream);
        }
    }

    @Override
    public SessionData decode(String data) {
        // TODO use java.util.Base64 from java 8
        byte[] bytes = DatatypeConverter.parseBase64Binary(data);

        ByteArrayInputStream inputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            inputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(inputStream);

            return (SessionData) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new PippoRuntimeException("Cannot deserialize session. A new one will be created.", e);
        } finally {
            IoUtils.close(objectInputStream);
            IoUtils.close(inputStream);
        }
    }

}
