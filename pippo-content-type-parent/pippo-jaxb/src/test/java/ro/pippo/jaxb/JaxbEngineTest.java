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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.sun.org.apache.xerces.internal.impl.Constants;

import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.IoUtils;

/**
 * @author Dwouglas Mhagnum
 */
public class JaxbEngineTest {

    private JaxbEngine jaxbEngine;

    @Before
    public void setUp() {
        System.out.println("vvvvvvvvvvvvvvvvvvvvvvv");

        Locale.setDefault(Locale.US);

//        String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
//        System.out.println("SECURITY_MANAGER: " + System.getProperty(SECURITY_MANAGER));
//        System.out.println("SECURITY_MANAGER: " + System.getSecurityManager());

        // com.sun.org.apache.xalan.internal.utils.XMLSecurityManager.Limit
        // com.sun.org.apache.xerces.internal.util.SecurityManager

        // Fix for Issue#586.  This limits entity expansion up to 100000 and nodes up to 3000.
        // setProperty(SECURITY_MANAGER, new org.apache.xerces.util.SecurityManager());

        // https://stackoverflow.com/questions/42991043/error-xml-sax-saxparseexception-while-parsing-a-xml-file-using-wikixmlj#43005865
        // -DentityExpansionLimit=2147480000 -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000

        // for QuadraticBlowupAttack
        // System.out.println("default jdk.xml.totalEntitySizeLimit: " + System.getProperty("jdk.xml.totalEntitySizeLimit"));
        // System.setProperty("jdk.xml.totalEntitySizeLimit", Integer.toString(Integer.MAX_VALUE));

        // Se extrapolar o jdk.xml.totalEntitySizeLimit , cai em:
        // Message: JAXP00010007: The total number of nodes in entity references is "3,000,001" that is over the limit "3,000,000" set by "FEATURE_SECURE_PROCESSING".
        // Que é controlado pelo jdk.xml.entityReplacementLimit
        // System.setProperty("jdk.xml.entityReplacementLimit", Integer.toString(Integer.MAX_VALUE));

        jaxbEngine = new JaxbEngine();

        jaxbEngine.setProperty(Constants.JDK_ENTITY_COUNT_INFO, "yes");

        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^");
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

        JaxbEngine jaxbEngine = new JaxbEngine();

        PippoRuntimeException exception = assertThrows(PippoRuntimeException.class, () -> {
            jaxbEngine.fromString(payload, Person.class);
        });

        assertThat(exception.getMessage(), startsWith("Failed to deserialize content to '"));

        Throwable rootCause = getRootCause(exception);
        assertThat(rootCause.getMessage(), containsString("JAXP00010001:"));
    }

    @Test
    public void test_fromString_QuadraticBlowupAttack() throws IOException {
        String payload = IoUtils.getResourceAsString("/attack_QuadraticBlowupAttack.xml");

        PippoRuntimeException exception = assertThrows(PippoRuntimeException.class, () -> {
            jaxbEngine.fromString(payload, Person.class);
        });

        assertThat(exception.getMessage(), startsWith("Failed to deserialize content to '"));

        Throwable rootCause = getRootCause(exception);
        assertThat(rootCause.getMessage(), containsString("JAXP00010004:"));
    }

    // TODO: move to an ExceptionUtils? Where?
    private Throwable getRootCause(Throwable t) {
        if (t.getCause() == null) {
            return t;
        }
        return getRootCause(t.getCause());
    }

}
