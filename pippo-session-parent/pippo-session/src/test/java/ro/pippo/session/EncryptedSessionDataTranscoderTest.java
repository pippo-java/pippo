/*
  * Copyright (C) 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.session;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Herman Barrantes
 */
public class EncryptedSessionDataTranscoderTest {

    /**
     * Test of encode and decode methods, of class EncryptorSessionDataTranscoder.
     */
    @Test
    public void test() {
        System.out.println("encode");
        SessionData sessionData = new DefaultSessionData();
        String sessionId = sessionData.getId();
        sessionData.put("KEY", "VALUE");
        EncryptedSessionDataTranscoder instance = new EncryptedSessionDataTranscoder.Builder().secretKey("PasswordPassword").build();
        String encrypted = instance.encode(sessionData);
        SessionData sessionDataDescrypted = instance.decode(encrypted);
        assertNotNull(sessionDataDescrypted);
        assertEquals(sessionDataDescrypted.getId(), sessionId);
        assertEquals(sessionDataDescrypted.get("KEY"), "VALUE");
    }

}
