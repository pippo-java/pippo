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
package ro.pippo.jackson;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * @author James Moger
 */
public abstract class JacksonBaseEngineTest extends Assert {

    protected abstract JacksonBaseEngine getEngine();

    @Test
    public void testEngine() {
        MyTest test = new MyTest();

        JacksonBaseEngine engine = getEngine();
        engine.init(null);

        String aString = engine.toString(test);

        MyTest result = engine.fromString(aString, MyTest.class);
        assertEquals(test.message, result.message);
    }

    @Test
    public void testDates() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        Date now = cal.getTime();

        MyTest test = new MyTest();
        test.date = now;

        JacksonBaseEngine engine = getEngine();
        engine.init(null);

        String aString = engine.toString(test);

        MyTest result = engine.fromString(aString, MyTest.class);
        assertEquals(test.message, result.message);

        assertTrue(test.date.equals(result.date));
    }

    public static class MyTest {

        public String message = "Hooray!";

        public Date date = new Date();

    }
}
