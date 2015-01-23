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
package ro.pippo.core;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author James Moger
 */
public class ParameterValueTest extends Assert {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Test
    public void testBooleans() throws Exception {
        assertEquals(true, new ParameterValue("true").toBoolean());
        assertEquals(true, new ParameterValue("true", "true", "true").toBoolean());
        assertArrayEquals(new Boolean[] {true, false, true}, new ParameterValue("true", "false", "true").to(Boolean[].class));
    }

    @Test
    public void testBytes() throws Exception {
        assertEquals(127, new ParameterValue("127").toByte());
        assertEquals(127, new ParameterValue("127", "96", "64").toByte());
        assertArrayEquals(new byte[] {127, 96, 64}, new ParameterValue("127", "96", "64").to(byte[].class));
    }

    @Test
    public void testShorts() throws Exception {
        assertEquals(4096, new ParameterValue("4096").toShort());
        assertEquals(4096, new ParameterValue("4096", "2048", "1024").toShort());
        assertArrayEquals(new short[] {4096, 2048, 1024}, new ParameterValue(new String [] { "4096", "2048", "1024" }).to(short[].class));
    }

    @Test
    public void testIntegers() throws Exception {
        assertEquals(131070, new ParameterValue("131070").toInt());
        assertEquals(131070, new ParameterValue("131070", "65535", "32767").toInt());
        assertArrayEquals(new int[] {131070, 65535, 32767}, new ParameterValue("131070", "65535", "32767").to(int[].class));
    }

    @Test
    public void testLongs() throws Exception {
        assertEquals(8589934588L, new ParameterValue("8589934588").toLong());
        assertEquals(8589934588L, new ParameterValue("8589934588", "4294967294", "2147483647").toLong());
        assertArrayEquals(new long[] {8589934588L, 4294967294L, 2147483647L}, new ParameterValue("8589934588", "4294967294", "2147483647").to(long[].class));
    }

    @Test
    public void testFloat() throws Exception {
        assertEquals(3.14159f, new ParameterValue("3.14159").toFloat(), 0f);
        assertEquals(3.14159f, new ParameterValue("3.14159", "3.14159", "3.14159").toFloat(), 0f);
        assertArrayEquals(new Float[] {3.14159f, 3.14159f, 3.14159f}, new ParameterValue("3.14159", "3.14159", "3.14159").to(Float[].class));
    }

    @Test
    public void testDouble() throws Exception {
        assertEquals(3.14159d, new ParameterValue("3.14159").toDouble(), 0d);
        assertEquals(3.14159d, new ParameterValue("3.14159", "3.14159", "3.14159").toDouble(), 0d);
        assertArrayEquals(new Double[] {3.14159d, 3.14159d, 3.14159d}, new ParameterValue("3.14159", "3.14159", "3.14159").to(Double[].class));
    }

    @Test
    public void testBigDecimal() throws Exception {
        assertEquals(new BigDecimal(3.14159d), new ParameterValue("3.14159").toBigDecimal());
        assertEquals(new BigDecimal(3.14159d), new ParameterValue("3.14159", "3.14159", "3.14159").toBigDecimal());
        assertArrayEquals(new BigDecimal[] {new BigDecimal(3.14159d), new BigDecimal(3.14159d), new BigDecimal(3.14159d)}, new ParameterValue("3.14159", "3.14159", "3.14159").to(BigDecimal[].class));
    }

    @Test
    public void testUUID() throws Exception {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        assertEquals(a, new ParameterValue(a.toString()).toUUID());
        assertEquals(a, new ParameterValue(a.toString(), b.toString(), c.toString()).toUUID());
        assertArrayEquals(new UUID [] { a, b, c}, new ParameterValue(a.toString(), b.toString(), c.toString()).to(UUID[].class));
    }

    @Test
    public void testCharacter() throws Exception {
        assertEquals(0, Character.compare('f', new ParameterValue("f").toCharacter()));
        assertEquals(0, Character.compare('f', new ParameterValue("fred", "wilma", "barney").toCharacter()));
        assertArrayEquals(new char[] { 'f', 'w', 'b'}, new ParameterValue("fred", "wilma", "barney").to(char[].class));
    }

    @Test
    public void testString() throws Exception {
        assertEquals("fred", new ParameterValue("fred").toString());
        assertEquals("fred", new ParameterValue("fred", "wilma", "barney").toString());
        assertArrayEquals(new String[] {"fred", "wilma", "barney"}, new ParameterValue("fred", "wilma", "barney").to(String[].class));
    }

    @Test
    public void testDate() throws Exception {
        assertEquals(Date.valueOf("2014-12-25"), new ParameterValue("2014-12-25").toDate("yyyy-MM-dd"));
        assertEquals(Date.valueOf("2014-12-25"), new ParameterValue("2014-12-25", "2015-12-25", "2016-12-25").toDate("yyyy-MM-dd"));
        assertArrayEquals(new Date[] {Date.valueOf("2014-12-25"), Date.valueOf("2015-12-25"), Date.valueOf("2016-12-25")}, new ParameterValue("2014-12-25", "2015-12-25", "2016-12-25").to(Date[].class, "yyyy-MM-dd"));
    }

    @Test
    public void testSqlDate() throws Exception {
        assertEquals(Date.valueOf("2014-12-25"), new ParameterValue("2014-12-25").toSqlDate());
        assertEquals(Date.valueOf("2014-12-25"), new ParameterValue("2014-12-25", "2015-12-25", "2016-12-25").toSqlDate());
        assertArrayEquals(new Date[] {Date.valueOf("2014-12-25"), Date.valueOf("2015-12-25"), Date.valueOf("2016-12-25")}, new ParameterValue("2014-12-25", "2015-12-25", "2016-12-25").to(Date[].class));
    }

    @Test
    public void testSqlTime() throws Exception {
        assertEquals(Time.valueOf("13:45:20"), new ParameterValue("13:45:20").toSqlTime());
        assertEquals(Time.valueOf("13:45:20"), new ParameterValue("13:45:20", "8:45:35", "20:45:07").toSqlTime());
        assertArrayEquals(new Time[] {Time.valueOf("13:45:20"), Time.valueOf("8:45:35"), Time.valueOf("20:45:07")}, new ParameterValue("13:45:20", "8:45:35", "20:45:07").to(Time[].class));
    }

    @Test
    public void testSqlTimestamp() throws Exception {
        assertEquals(Timestamp.valueOf("2014-12-25 13:45:20"), new ParameterValue("2014-12-25 13:45:20").toSqlTimestamp());
        assertEquals(Timestamp.valueOf("2014-12-25 13:45:20"), new ParameterValue("2014-12-25 13:45:20", "2014-12-25 8:45:35", "2014-12-25 20:45:07").toSqlTimestamp());
        assertArrayEquals(new Timestamp[] {Timestamp.valueOf("2014-12-25 13:45:20"), Timestamp.valueOf("2014-12-25 8:45:35"), Timestamp.valueOf("2014-12-25 20:45:07")}, new ParameterValue("2014-12-25 13:45:20", "2014-12-25 8:45:35", "2014-12-25 20:45:07").to(Timestamp[].class));
    }


    @Test
    public void testEnums() throws Exception {
        assertEquals(Alphabet.B, new ParameterValue("B").toEnum(Alphabet.class));
        assertEquals(Alphabet.B, new ParameterValue("B", "A", "D").toEnum(Alphabet.class));
        assertArrayEquals(new Alphabet[] {Alphabet.B, Alphabet.A, Alphabet.D}, new ParameterValue("B", "A", "D").to(Alphabet[].class));

        assertEquals(Alphabet.B, new ParameterValue("b").toEnum(Alphabet.class, null, false));
        assertNull(new ParameterValue("z").toEnum(Alphabet.class, null, false));
        assertEquals(Alphabet.B, new ParameterValue("z").toEnum(Alphabet.class, Alphabet.B, false));

        assertEquals(Alphabet.B, new ParameterValue("1").toEnum(Alphabet.class, null, false));
        assertEquals(Alphabet.A, new ParameterValue("0").toEnum(Alphabet.class, null, false));
        assertEquals(Alphabet.D, new ParameterValue("3").toEnum(Alphabet.class, null, false));

    }

    public static enum Alphabet {
        A, B, C, D, E, F, G
    }
}
