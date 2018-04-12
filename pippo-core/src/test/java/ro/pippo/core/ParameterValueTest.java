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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author James Moger
 */
public class ParameterValueTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testBooleans() throws Exception {
        assertEquals(false, new ParameterValue("").toBoolean());
        assertEquals(false, new ParameterValue(" ").toBoolean());
        assertEquals(true, new ParameterValue("true").toBoolean());
        assertEquals(true, new ParameterValue("true", "true", "true").toBoolean());
        assertEquals(true, new ParameterValue("yes").toBoolean());
        assertEquals(true, new ParameterValue("on").toBoolean());
        assertEquals(true, new ParameterValue("1").toBoolean());
        assertEquals(true, new ParameterValue("2").toBoolean());
        assertEquals(false, new ParameterValue("0").toBoolean());
        assertArrayEquals(new Boolean[]{true, false, true}, new ParameterValue("true", "false", "true").to(Boolean[].class));
        assertArrayEquals(new Boolean[]{true, true, true}, new ParameterValue("true", "yes", "on").to(Boolean[].class));
    }

    @Test
    public void testBytes() throws Exception {
        assertEquals(0, new ParameterValue("").toByte());
        assertEquals(0, new ParameterValue(" ").toByte());
        assertEquals(127, new ParameterValue("127").toByte());
        assertEquals(127, new ParameterValue("127", "96", "64").toByte());
        assertArrayEquals(new byte[]{127, 96, 64}, new ParameterValue("127", "96", "64").to(byte[].class));
    }

    @Test
    public void testShorts() throws Exception {
        assertEquals(0, new ParameterValue("").toShort());
        assertEquals(0, new ParameterValue(" ").toShort());
        assertEquals(4096, new ParameterValue("4096").toShort());
        assertEquals(4096, new ParameterValue("4096", "2048", "1024").toShort());
        assertArrayEquals(new short[]{4096, 2048, 1024}, new ParameterValue(new String[]{"4096", "2048", "1024"}).to(short[].class));
    }

    @Test
    public void testIntegers() throws Exception {
        assertEquals(0, new ParameterValue("").toInt());
        assertEquals(0, new ParameterValue(" ").toInt());
        assertEquals(131070, new ParameterValue("131070").toInt());
        assertEquals(131070, new ParameterValue("131070", "65535", "32767").toInt());
        assertArrayEquals(new int[]{131070, 65535, 32767}, new ParameterValue("131070", "65535", "32767").to(int[].class));
    }

    @Test
    public void testLongs() throws Exception {
        assertEquals(0L, new ParameterValue("").toLong());
        assertEquals(0L, new ParameterValue(" ").toLong());
        assertEquals(8589934588L, new ParameterValue("8589934588").toLong());
        assertEquals(8589934588L, new ParameterValue("8589934588", "4294967294", "2147483647").toLong());
        assertArrayEquals(new long[]{8589934588L, 4294967294L, 2147483647L}, new ParameterValue("8589934588", "4294967294", "2147483647").to(long[].class));
    }

    @Test
    public void testFloatUS() throws Exception {
        final Locale US = Locale.US;
        assertEquals(0f, new ParameterValue(US, "").toFloat(), 0f);
        assertEquals(0f, new ParameterValue(US, " ").toFloat(), 0f);
        assertEquals(3.14159f, new ParameterValue(US, "3.14159").toFloat(), 0f);
        assertEquals(3.14159f, new ParameterValue(US, "3.14159", "3.14159", "3.14159").toFloat(), 0f);
        assertArrayEquals(new Float[]{3.14159f, 3.14159f, 3.14159f}, new ParameterValue(US, "3.14159", "3.14159", "3.14159").to(Float[].class));
    }

    @Test
    public void testFloatPtBR() throws Exception {
        final Locale PT_BR = new Locale("pt", "BR");
        assertEquals(0f, new ParameterValue(PT_BR, "").toFloat(), 0f);
        assertEquals(0f, new ParameterValue(PT_BR, " ").toFloat(), 0f);
        assertEquals(3.14159f, new ParameterValue(PT_BR, "3,14159").toFloat(), 0f);
        assertEquals(3.14159f, new ParameterValue(PT_BR, "3,14159", "3,14159", "3,14159").toFloat(), 0f);
        assertArrayEquals(new Float[]{3.14159f, 3.14159f, 3.14159f}, new ParameterValue(PT_BR, "3,14159", "3,14159", "3,14159").to(Float[].class));
    }

    @Test
    public void testFloatFrench() throws Exception {
        assertEquals(0f, new ParameterValue(Locale.FRENCH, "").toFloat(), 0f);
        assertEquals(0f, new ParameterValue(Locale.FRENCH, " ").toFloat(), 0f);
        // grouping separator for the French locale to be used should be \u00a0 (non-breaking space) not \u0020 (space)
        final String frenchNumber = "3 987,14159".replaceAll(" ", "\u00A0");
        assertEquals(3987.14159f, new ParameterValue(Locale.FRENCH, frenchNumber).toFloat(), 0f);
        assertEquals(3987.14159f, new ParameterValue(Locale.FRENCH, frenchNumber, frenchNumber, frenchNumber).toFloat(), 0f);
        assertArrayEquals(new Float[]{3987.14159f, 3987.14159f, 3987.14159f}, new ParameterValue(Locale.FRENCH, frenchNumber, frenchNumber, frenchNumber).to(Float[].class));
    }

    @Test
    public void testFloatDefaultLocale() throws Exception {
        assertEquals(0f, new ParameterValue("").toFloat(), 0f);
        assertEquals(0f, new ParameterValue(" ").toFloat(), 0f);

        {
            final float expected = 3.14159f;
            final String numberString = formatFloatToDefaultLocale(expected);
            assertEquals(expected, new ParameterValue(numberString).toFloat(), 0f);
            assertEquals(expected, new ParameterValue(numberString, numberString, numberString).toFloat(), 0f);
            assertArrayEquals(new Float[]{expected, expected, expected}, new ParameterValue(numberString, numberString, numberString).to(Float[].class));
        }

        {
            final float expected = 1.00f; // same 1.0f or 1f
            final String numberString = formatFloatToDefaultLocale(expected);
            assertEquals(expected, new ParameterValue(numberString).toFloat(), 0f);
            assertEquals(expected, new ParameterValue(numberString, numberString, numberString).toFloat(), 0f);
            assertArrayEquals(new Float[]{expected, expected, expected}, new ParameterValue(numberString, numberString, numberString).to(Float[].class));
        }
    }

    @Test
    public void testDoubleUS() throws Exception {
        final Locale US = Locale.US;
        assertEquals(0d, new ParameterValue(US, "").toDouble(), 0d);
        assertEquals(0d, new ParameterValue(US, " ").toDouble(), 0d);
        assertEquals(3.14159d, new ParameterValue(US, "3.14159").toDouble(), 0d);
        assertEquals(3.14159d, new ParameterValue(US, "3.14159", "3.14159", "3.14159").toDouble(), 0d);
        assertArrayEquals(new Double[]{3.14159d, 3.14159d, 3.14159d}, new ParameterValue(US, "3.14159", "3.14159", "3.14159").to(Double[].class));
    }

    @Test
    public void testDoublePtBR() throws Exception {
        final Locale PT_BR = new Locale("pt", "BR");
        assertEquals(0d, new ParameterValue(PT_BR, "").toDouble(), 0d);
        assertEquals(0d, new ParameterValue(PT_BR, " ").toDouble(), 0d);
        assertEquals(3.14159d, new ParameterValue(PT_BR, "3,14159").toDouble(), 0d);
        assertEquals(3.14159d, new ParameterValue(PT_BR, "3,14159", "3,14159", "3,14159").toDouble(), 0d);
        assertArrayEquals(new Double[]{3.14159d, 3.14159d, 3.14159d}, new ParameterValue(PT_BR, "3,14159", "3,14159", "3,14159").to(Double[].class));
    }

    @Test
    public void testDoubleFrench() throws Exception {
        assertEquals(0d, new ParameterValue(Locale.FRENCH, "").toDouble(), 0d);
        assertEquals(0d, new ParameterValue(Locale.FRENCH, " ").toDouble(), 0d);
        // grouping separator for the French locale to be used should be \u00a0 (non-breaking space) not \u0020 (space)
        final String frenchNumber = "3 987,14159".replaceAll(" ", "\u00A0");
        assertEquals(3987.14159d, new ParameterValue(Locale.FRENCH, frenchNumber).toDouble(), 0d);
        assertEquals(3987.14159d, new ParameterValue(Locale.FRENCH, frenchNumber, frenchNumber, frenchNumber).toDouble(), 0d);
        assertArrayEquals(new Double[]{3987.14159d, 3987.14159d, 3987.14159d}, new ParameterValue(Locale.FRENCH, frenchNumber, frenchNumber, frenchNumber).to(Double[].class));
    }

    @Test
    public void testDoubleDefaultLocale() throws Exception {
        assertEquals(0d, new ParameterValue("").toDouble(), 0d);
        assertEquals(0d, new ParameterValue(" ").toDouble(), 0d);

        {
            final double expected = 3.14159d;
            final String numberString = formatDoubleToDefaultLocale(expected);
            assertEquals(expected, new ParameterValue(numberString).toDouble(), 0d);
            assertEquals(expected, new ParameterValue(numberString, numberString, numberString).toDouble(), 0d);
            assertArrayEquals(new Double[]{expected, expected, expected}, new ParameterValue(numberString, numberString, numberString).to(Double[].class));
        }

        {
            final double expected = 1.00d; // same 1.0d or 1d
            final String numberString = formatDoubleToDefaultLocale(expected);
            assertEquals(expected, new ParameterValue(numberString).toDouble(), 0d);
            assertEquals(expected, new ParameterValue(numberString, numberString, numberString).toDouble(), 0d);
            assertArrayEquals(new Double[]{expected, expected, expected}, new ParameterValue(numberString, numberString, numberString).to(Double[].class));
        }
    }

    @Test
    public void testBigDecimalUS() throws Exception {
        final Locale US = Locale.US;
        assertEquals(new BigDecimal(0d), new ParameterValue(US, "").toBigDecimal());
        assertEquals(new BigDecimal(0d), new ParameterValue(US, " ").toBigDecimal());
        assertEquals(new BigDecimal("3.14159"), new ParameterValue(US, "3.14159").toBigDecimal());
        assertEquals(new BigDecimal("3.14159"), new ParameterValue(US, "3.14159", "3.14159", "3.14159").toBigDecimal());
        assertArrayEquals(new BigDecimal[]{new BigDecimal("3.14159"), new BigDecimal("3.14159"), new BigDecimal("3.14159")}, new ParameterValue(US, "3.14159", "3.14159", "3.14159").to(BigDecimal[].class));
    }

    @Test
    public void testBigDecimalPtBR() throws Exception {
        final Locale PT_BR = new Locale("pt", "BR");
        assertEquals(new BigDecimal(0d), new ParameterValue(PT_BR, "").toBigDecimal());
        assertEquals(new BigDecimal(0d), new ParameterValue(PT_BR, " ").toBigDecimal());
        assertEquals(new BigDecimal("3.14159"), new ParameterValue(PT_BR, "3,14159").toBigDecimal());
        assertEquals(new BigDecimal("3.14159"), new ParameterValue(PT_BR, "3,14159", "3,14159", "3,14159").toBigDecimal());
        assertArrayEquals(new BigDecimal[]{new BigDecimal("3.14159"), new BigDecimal("3.14159"), new BigDecimal("3.14159")}, new ParameterValue(PT_BR, "3,14159", "3,14159", "3,14159").to(BigDecimal[].class));
    }

    @Test
    public void testBigDecimalFrench() throws Exception {
        assertEquals(new BigDecimal(0d), new ParameterValue(Locale.FRENCH, "").toBigDecimal());
        assertEquals(new BigDecimal(0d), new ParameterValue(Locale.FRENCH, " ").toBigDecimal());
        // grouping separator for the French locale to be used should be \u00a0 (non-breaking space) not \u0020 (space)
        final String frenchNumber = "3 987,14159".replaceAll(" ", "\u00A0");
        assertEquals(new BigDecimal("3987.14159"), new ParameterValue(Locale.FRENCH, frenchNumber).toBigDecimal());
        assertEquals(new BigDecimal("3987.14159"), new ParameterValue(Locale.FRENCH, frenchNumber, frenchNumber, frenchNumber).toBigDecimal());
        assertArrayEquals(new BigDecimal[]{new BigDecimal("3987.14159"), new BigDecimal("3987.14159"), new BigDecimal("3987.14159")}, new ParameterValue(Locale.FRENCH, frenchNumber, frenchNumber, frenchNumber).to(BigDecimal[].class));
    }

    @Test
    public void testBigDecimalDefaultLocale() throws Exception {
        assertEquals(new BigDecimal(0d), new ParameterValue("").toBigDecimal());
        assertEquals(new BigDecimal(0d), new ParameterValue(" ").toBigDecimal());

        {
            final BigDecimal expected = new BigDecimal("3.14159");
            final String numberString = formatBigDecimalToDefaultLocale(expected);
            assertEquals(expected, new ParameterValue(numberString).toBigDecimal());
            assertEquals(expected, new ParameterValue(numberString, numberString, numberString).toBigDecimal());
            assertArrayEquals(new BigDecimal[]{expected, expected, expected}, new ParameterValue(numberString, numberString, numberString).to(BigDecimal[].class));
        }

        {
            final BigDecimal expected = new BigDecimal("1.00");
            final String numberString = formatBigDecimalToDefaultLocale(expected);
            assertEquals(expected, new ParameterValue(numberString).toBigDecimal());
            assertEquals(expected, new ParameterValue(numberString, numberString, numberString).toBigDecimal());
            assertArrayEquals(new BigDecimal[]{expected, expected, expected}, new ParameterValue(numberString, numberString, numberString).to(BigDecimal[].class));
        }
    }

    @Test
    public void testUUID() throws Exception {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        assertNull(new ParameterValue("").toUUID());
        assertNull(new ParameterValue(" ").toUUID());
        assertEquals(a, new ParameterValue(a.toString()).toUUID());
        assertEquals(a, new ParameterValue(a.toString(), b.toString(), c.toString()).toUUID());
        assertArrayEquals(new UUID[]{a, b, c}, new ParameterValue(a.toString(), b.toString(), c.toString()).to(UUID[].class));
    }

    @Test
    public void testCharacter() throws Exception {
        assertEquals(0, Character.compare((char) 0, new ParameterValue("").toCharacter()));
        assertEquals(0, Character.compare(' ', new ParameterValue(" ").toCharacter()));
        assertEquals(0, Character.compare('f', new ParameterValue("f").toCharacter()));
        assertEquals(0, Character.compare('f', new ParameterValue("fred", "wilma", "barney").toCharacter()));
        assertArrayEquals(new char[]{'f', 'w', 'b'}, new ParameterValue("fred", "wilma", "barney").to(char[].class));
    }

    @Test
    public void testString() throws Exception {
        assertEquals("métier", new ParameterValue("métier").toString());
        assertEquals("fred", new ParameterValue("fred").toString());
        assertEquals("fred", new ParameterValue("fred", "wilma", "barney").toString());
        assertArrayEquals(new String[]{"fred", "wilma", "barney"}, new ParameterValue("fred", "wilma", "barney").to(String[].class));
    }

    @Test
    public void testDate() throws Exception {
        assertNull(new ParameterValue("").toDate("yyyy-MM-dd"));
        assertNull(new ParameterValue(" ").toDate("yyyy-MM-dd"));
        assertEquals(Date.valueOf("2014-12-25"), new ParameterValue("2014-12-25").toDate("yyyy-MM-dd"));
        assertEquals(Date.valueOf("2014-12-25"), new ParameterValue("2014-12-25", "2015-12-25", "2016-12-25").toDate("yyyy-MM-dd"));
        assertArrayEquals(new Date[]{Date.valueOf("2014-12-25"), Date.valueOf("2015-12-25"), Date.valueOf("2016-12-25")}, new ParameterValue("2014-12-25", "2015-12-25", "2016-12-25").to(Date[].class, "yyyy-MM-dd"));
    }

    @Test
    public void testSqlDate() throws Exception {
        assertNull(new ParameterValue("").toSqlDate());
        assertNull(new ParameterValue(" ").toSqlDate());
        assertEquals(Date.valueOf("2014-12-25"), new ParameterValue("2014-12-25").toSqlDate());
        assertEquals(Date.valueOf("2014-12-25"), new ParameterValue("2014-12-25", "2015-12-25", "2016-12-25").toSqlDate());
        assertArrayEquals(new Date[]{Date.valueOf("2014-12-25"), Date.valueOf("2015-12-25"), Date.valueOf("2016-12-25")}, new ParameterValue("2014-12-25", "2015-12-25", "2016-12-25").to(Date[].class));
    }

    @Test
    public void testSqlTime() throws Exception {
        assertNull(new ParameterValue("").toSqlTime());
        assertNull(new ParameterValue(" ").toSqlTime());
        assertEquals(Time.valueOf("13:45:20"), new ParameterValue("13:45:20").toSqlTime());
        assertEquals(Time.valueOf("13:45:20"), new ParameterValue("13:45:20", "8:45:35", "20:45:07").toSqlTime());
        assertArrayEquals(new Time[]{Time.valueOf("13:45:20"), Time.valueOf("8:45:35"), Time.valueOf("20:45:07")}, new ParameterValue("13:45:20", "8:45:35", "20:45:07").to(Time[].class));
    }

    @Test
    public void testSqlTimestamp() throws Exception {
        assertNull(new ParameterValue("").toSqlTimestamp());
        assertNull(new ParameterValue(" ").toSqlTimestamp());
        assertEquals(Timestamp.valueOf("2014-12-25 13:45:20"), new ParameterValue("2014-12-25 13:45:20").toSqlTimestamp());
        assertEquals(Timestamp.valueOf("2014-12-25 13:45:20"), new ParameterValue("2014-12-25 13:45:20", "2014-12-25 8:45:35", "2014-12-25 20:45:07").toSqlTimestamp());
        assertArrayEquals(new Timestamp[]{Timestamp.valueOf("2014-12-25 13:45:20"), Timestamp.valueOf("2014-12-25 8:45:35"), Timestamp.valueOf("2014-12-25 20:45:07")}, new ParameterValue("2014-12-25 13:45:20", "2014-12-25 8:45:35", "2014-12-25 20:45:07").to(Timestamp[].class));
    }

    @Test
    public void testStringList() throws Exception {
        assertEquals(Arrays.asList("A", "B", "C"), new ParameterValue("A", "B", "C").toList());
    }

    @Test
    public void testIntegerList() throws Exception {
        assertEquals(Arrays.asList(200, 400, 600), new ParameterValue("200", "400", "600").toList(Integer.class));
    }

    @Test
    public void testEmptySet() throws Exception {
        assertEquals(Collections.emptySet(), new ParameterValue("").toSet());
        assertEquals(Collections.emptySet(), new ParameterValue("  ").toSet());
        assertEquals(Collections.emptySet(), new ParameterValue(" ").toSet(Integer.class));
    }

    @Test
    public void testStringHashSet() throws Exception {
        Set<String> mySet = new HashSet<>(Arrays.asList("A", "B", "C"));
        Set<String> targetSet = new ParameterValue("C", "B", "A").toSet(String.class);
        assertEquals(mySet, targetSet);
    }

    @Test
    public void testIntegerHashSet() throws Exception {
        Set<Integer> mySet = new HashSet<>(Arrays.asList(200, 400, 600));
        assertEquals(mySet, new ParameterValue("600", "200", "400", "200").toSet(Integer.class));
    }

    @Test
    public void testEncodedHashSet() throws Exception {
        Set<Integer> mySet = new HashSet<>(Arrays.asList(200, 400, 600));
        assertEquals(mySet, new ParameterValue("[600,200, 400,200]").toSet(Integer.class));
    }

    @Test
    public void testHashSet() throws Exception {
        Set<String> mySet = new HashSet<>(Arrays.asList("200", "400", "600"));
        assertEquals(mySet, new ParameterValue("600", "200", "400", "200").toSet());

        // when values contains single entry
        mySet = new HashSet<>(Arrays.asList("200"));
        assertEquals(mySet, new ParameterValue("200").toSet());
    }

    @Test
    public void testStringTreeSet() throws Exception {
        TreeSet<String> mySet = new TreeSet<>(Arrays.asList("C", "B", "A"));
        assertEquals(mySet, new ParameterValue("C", "A", "B", "A").toCollection(TreeSet.class, String.class, null));
    }

    @Test
    public void testIntegerTreeSet() throws Exception {
        TreeSet<Integer> mySet = new TreeSet<>(Arrays.asList(600, 200, 400, 200));
        assertEquals(mySet, new ParameterValue("600", "200", "400", "200").toCollection(TreeSet.class, Integer.class, null));
    }

    @Test
    public void testEncodedTreeSet() throws Exception {
        TreeSet<Integer> mySet = new TreeSet<>(Arrays.asList(600, 200, 400, 200));
        assertEquals(mySet, new ParameterValue("[600, 400, 200]").toCollection(TreeSet.class, Integer.class, null));
    }

    @Test
    public void testEmptyArrayList() throws Exception {
        assertEquals(Collections.emptyList(), new ParameterValue("").toList());
        assertEquals(Collections.emptyList(), new ParameterValue("  ").toList());
        assertEquals(Collections.emptyList(), new ParameterValue(" ").toList(Integer.class));
    }

    @Test
    public void testStringArrayList() throws Exception {
        List<String> myList = new ArrayList<>(Arrays.asList("C", "B", "A"));
        assertEquals(myList, new ParameterValue("C", "B", "A").toList(String.class));
    }

    @Test
    public void testIntegerArrayList() throws Exception {
        List<Integer> myList = new ArrayList<>(Arrays.asList(600, 400, 200));
        assertEquals(myList, new ParameterValue("600", "400", "200").toList(Integer.class));
    }

    @Test
    public void testEncodedArrayList() throws Exception {
        List<Integer> myList = new ArrayList<>(Arrays.asList(600, 400, 200));
        assertEquals(myList, new ParameterValue("[600, 400, 200]").toList(Integer.class));
    }

    @Test
    public void testEncodedArrayList2() throws Exception {
        List<Integer> myList = new ArrayList<>(Arrays.asList(600, 400, 200));
        assertEquals(myList, new ParameterValue("600, 400,200").toList(Integer.class));
    }

    @Test
    public void testEncodedArrayList3() throws Exception {
        List<Integer> myList = new ArrayList<>(Arrays.asList(600, 400, 200));
        assertEquals(myList, new ParameterValue("600| 400|200").toList(Integer.class));
    }

    @Test
    public void testEncodedArray() throws Exception {
        int [] myArray = { 600, 400, 200 };
        assertTrue(Arrays.equals(myArray, new ParameterValue("[600, 400, 200]").to(int[].class)));
    }

    @Test
    public void testEncodedArray2() throws Exception {
        int [] myArray = { 600, 400, 200 };
        assertTrue(Arrays.equals(myArray, new ParameterValue("600, 400,200").to(int[].class)));
    }

    @Test
    public void testEncodedArray3() throws Exception {
        int [] myArray = { 600, 400, 200 };
        assertTrue(Arrays.equals(myArray, new ParameterValue("600| 400|200").to(int[].class)));
    }

    @Test
    public void testEmptyArray() throws Exception {
        // when parameterValue is empty
        assertTrue(Arrays.equals(new int[0], new ParameterValue("").to(int[].class)));
        // when parameterValue is null
        assertTrue(Arrays.equals(new int[0], new ParameterValue().to(int[].class)));
    }

    @Test
    public void testEnums() throws Exception {
        assertNull(new ParameterValue(" ").toEnum(Alphabet.class));
        assertEquals(Alphabet.B, new ParameterValue("B").toEnum(Alphabet.class));
        assertEquals(Alphabet.B, new ParameterValue("B", "A", "D").toEnum(Alphabet.class));
        assertArrayEquals(new Alphabet[]{Alphabet.B, Alphabet.A, Alphabet.D}, new ParameterValue("B", "A", "D").to(Alphabet[].class));

        assertEquals(Alphabet.B, new ParameterValue("b").toEnum(Alphabet.class, null, false));
        assertNull(new ParameterValue("z").toEnum(Alphabet.class, null, false));
        assertEquals(Alphabet.B, new ParameterValue("z").toEnum(Alphabet.class, Alphabet.B, false));

        assertEquals(Alphabet.B, new ParameterValue("1").toEnum(Alphabet.class, null, false));
        assertEquals(Alphabet.A, new ParameterValue("0").toEnum(Alphabet.class, null, false));
        assertEquals(Alphabet.D, new ParameterValue("3").toEnum(Alphabet.class, null, false));
    }

    private enum Alphabet {
        A, B, C, D, E, F, G
    }

    private String formatBigDecimalToDefaultLocale(BigDecimal number) {
        final DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
        formatter.setMinimumFractionDigits(number.scale());
        formatter.setMaximumFractionDigits(number.scale());
        return formatter.format(number);
    }

    private String formatFloatToDefaultLocale(float number) {
        return formatBigDecimalToDefaultLocale(new BigDecimal(Float.toString(number)));
    }

    private String formatDoubleToDefaultLocale(double number) {
        return formatBigDecimalToDefaultLocale(new BigDecimal(Double.toString(number)));
    }

}
