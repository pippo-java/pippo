/*
 * Copyright (C) 2018 the original author or authors.
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
package ro.pippo.core;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PippoSettingsTest {

    private PippoSettings pippoSettings = Mockito.mock(PippoSettings.class);

    @Test
    public void testGetStrings() {
        Mockito.doReturn(" [value1, value2, value3]").when(pippoSettings)
            .getString("key", null);
        Mockito.doCallRealMethod().when(pippoSettings).getStrings("key", ",");

        // using default delimiter, since it is private pass same as delimiter
        List<String> values = pippoSettings.getStrings("key", ",");
        assertEquals(Arrays.asList("value1","value2","value3"), values);

        // when only closing bracket specified
        Mockito.doReturn(" value1/value2/value3]").when(pippoSettings)
            .getString("key", null);
        Mockito.doCallRealMethod().when(pippoSettings).getStrings("key", "/");
        values = pippoSettings.getStrings("key", "/");
        assertEquals(Arrays.asList("value1","value2","value3]"), values);
    }

    @Test
    public void testGetIntegers() {
        Mockito.doReturn(" [1234, 123, value3]").when(pippoSettings)
            .getString("key", null);
        Mockito.doCallRealMethod().when(pippoSettings).getStrings("key", ",");
        Mockito.doCallRealMethod().when(pippoSettings).getIntegers("key", ",");

        List<Integer> values = pippoSettings.getIntegers("key", ",");
        assertEquals(Arrays.asList(1234, 123), values);

        Mockito.doReturn(Collections.emptyList()).when(pippoSettings).getStrings("key", ",");
        values = pippoSettings.getIntegers("key", ",");
        assertTrue(values.isEmpty());
    }

    @Test
    public void testGetDoubles() {
        // tests would similar for getFloats(String, String)
        Mockito.doReturn(" [1234, 123, value3]").when(pippoSettings)
            .getString("key", null);
        Mockito.doCallRealMethod().when(pippoSettings).getStrings("key", ",");
        Mockito.doCallRealMethod().when(pippoSettings).getDoubles("key", ",");

        List<Double> values = pippoSettings.getDoubles("key", ",");
        assertEquals(Arrays.asList(1234d, 123d), values);

        Mockito.doReturn(Collections.emptyList()).when(pippoSettings).getStrings("key", ",");
        values = pippoSettings.getDoubles("key", ",");
        assertTrue(values.isEmpty());
    }

    @Test
    public void testGetNumber() {
        Mockito.doReturn(" 1234").when(pippoSettings).getString("key", null);
        Mockito.doCallRealMethod().when(pippoSettings).getInteger("key", 0);
        Mockito.doCallRealMethod().when(pippoSettings).getLong("key", 0);
        Mockito.doCallRealMethod().when(pippoSettings).getFloat("key", 0.0f);
        Mockito.doCallRealMethod().when(pippoSettings).getDouble("key", 2.4d);

        int valueInt = pippoSettings.getInteger("key", 0);
        long valueLong = pippoSettings.getLong("key", 0);
        float valueFloat = pippoSettings.getFloat("key", 0.0f);
        double valueDouble = pippoSettings.getDouble("key", 2.4d);

        assertEquals(1234, valueInt);
        assertEquals(1234L, valueLong);
        assertEquals(Float.parseFloat("1234"), valueFloat, 0.0f);
        assertEquals(Double.parseDouble("1234"), valueDouble, 0.0d);

        // case when number followed by some char sequence
        Mockito.doReturn(" 1234 abc").when(pippoSettings).getString("key", null);
        valueInt = pippoSettings.getInteger("key", 0);
        valueLong = pippoSettings.getLong("key", 0);
        valueFloat = pippoSettings.getFloat("key", 0.0f);
        valueDouble = pippoSettings.getDouble("key", 2.4d);

        assertEquals(0, valueInt);
        assertEquals(0L, valueLong);
        assertEquals(0.0f, valueFloat, 0.0f);
        assertEquals(2.4d, valueDouble, 0.0d);
    }

}
