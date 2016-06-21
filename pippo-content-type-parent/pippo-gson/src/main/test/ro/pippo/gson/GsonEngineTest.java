/*
 * Copyright (C) 2014 the original author or authors.
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
package ro.pippo.gson;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * @author James Moger
 */
public class GsonEngineTest extends Assert {

    @Test
    public void testEngine() {
        MyTest test = new MyTest();

        GsonEngine engine = new GsonEngine();
        String json = engine.toString(test);

        MyTest result = engine.fromString(json, MyTest.class);
        assertEquals(test.message, result.message);
    }

    @Test
    public void testDates() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        Date now = cal.getTime();

        MyTest test = new MyTest();
        test.date = now;

        GsonEngine engine = new GsonEngine();
        String json = engine.toString(test);

        MyTest result = engine.fromString(json, MyTest.class);
        assertEquals(test.message, result.message);

        assertTrue(test.date.equals(result.date));
    }

    public static class MyTest {

        public String message = "Hooray!";

        public Date date = new Date();

    }
}
