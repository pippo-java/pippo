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
}
