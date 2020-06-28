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
package ro.pippo.jaxb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.sun.org.apache.xerces.internal.impl.Constants;

import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.IoUtils;

/**
 * @author Dwouglas Mhagnum
 */
public class JaxbEngineTest {

    private JaxbEngine jaxbEngine;

    @Rule
    public TestRule watcher = new TestWatcher() {
       protected void starting(Description description) {
          System.out.println(String.format("[%s] Starting test: %s", this.getClass().getName(), description.getMethodName()));
       }
    };

    @Before
    public void setUp() {
        jaxbEngine = new JaxbEngine();

        // JDK property to allow printing out information from the limit analyzer
        jaxbEngine.setProperty(Constants.JDK_ENTITY_COUNT_INFO, "yes");
    }

    @Test
    public void test_fromString_noDTD() throws IOException {
        String payload = IoUtils.getResourceAsString("/regular_no_dtd.xml");

        Person p = jaxbEngine.fromString(payload, Person.class);

        assertThat(p, isA(Person.class));
        assertEquals("Pippo", p.name);
    }

    @Test
    public void test_fromString_DTD() throws IOException {
        String payload = IoUtils.getResourceAsString("/regular_dtd.xml");

        Person p = jaxbEngine.fromString(payload, Person.class);

        assertThat(p, isA(Person.class));
        assertEquals("Pippo", p.name);
    }

    @Test
    public void test_fromString_BillionLaughsAttack() throws IOException {
        String payload = IoUtils.getResourceAsString("/attack_BillionLaughsAttack.xml");

        PippoRuntimeException exception = assertThrows(PippoRuntimeException.class, () -> {
            jaxbEngine.fromString(payload, Person.class);
        });

        assertThat(exception.getMessage(), startsWith("Failed to deserialize content to '"));
    }

    @Test
    public void test_fromString_QuadraticBlowupAttack() throws IOException {
        String payload = IoUtils.getResourceAsString("/attack_QuadraticBlowupAttack.xml");

        PippoRuntimeException exception = assertThrows(PippoRuntimeException.class, () -> {
            jaxbEngine.fromString(payload, Person.class);
        });

        assertThat(exception.getMessage(), startsWith("Failed to deserialize content to '"));
    }

}
