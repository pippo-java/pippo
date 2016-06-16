/*
 * Copyright (C) 2016 the original author or authors.
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
package ro.pippo.csv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.junit.Test;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author James Moger
 */
public class CsvEngineTest {

    @Test(expected = RuntimeException.class)
    public void testToCollection() {
        CsvEngine csvEngine = new CsvEngine();
        csvEngine.fromString(expected, ArrayList.class);
    }

    @Test
    public void testFromCollection() {
        CsvEngine csvEngine = new CsvEngine();
        String generated = csvEngine.toString(Arrays.asList(Product.get())).replace("\r\n", "\n").trim();

        assertEquals("Generated CSV is not the same", expected, generated);
    }

    @Test
    public void testFromArray() {
        CsvEngine csvEngine = new CsvEngine();
        String generated = csvEngine.toString(Product.get()).replace("\r\n", "\n").trim();

        assertEquals("Generated CSV is not the same", expected, generated);
    }

    @Test
    public void testToArray() {
        CsvEngine csvEngine = new CsvEngine();
        Product[] products = csvEngine.fromString(expected, Product[].class);
        assertEquals("Products are not the same", Arrays.toString(Product.get()), Arrays.toString(products));
    }

    final String expected = "id,sku,description,uuid,lastRestock\n" +
        "1,12345,Oranges,a0765bce-5d81-4b7c-8d28-b7f1efcb355b,2016-12-12\n" +
        "2,12345,Apples,25bc5325-6ce0-4e68-a5d9-4a5cfc6fb148,2015-12-12\n" +
        "3,12345,Bananas,e73b5a3d-e244-4b91-ae00-8fb6bb2fa91e,2014-12-12\n" +
        "4,12345,Peaches,1a4ff82a-7899-4f6b-b46d-883040a375a7,2013-12-12\n" +
        "5,12345,Pears,b3d0ad20-f16e-47ac-bc67-a05f2695838b,2012-12-12";

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Product implements Csv {

        long id;

        String sku;

        String description;

        UUID uuid;

        Date lastRestock;

        @Override
        public String[] getCsvHeader() {
            return new String[]{"id", "sku", "description", "uuid", "lastRestock"};
        }

        @Override
        public Object[] getCsvData() {
            return new Object[]{id, sku, description, uuid, lastRestock};
        }

        public static Product[] get() {
            return new Product[]{
                Product.builder().id(1).sku("12345").description("Oranges").uuid(UUID.fromString("a0765bce-5d81-4b7c-8d28-b7f1efcb355b")).lastRestock(Date.valueOf("2016-12-12")).build(),
                Product.builder().id(2).sku("12345").description("Apples").uuid(UUID.fromString("25bc5325-6ce0-4e68-a5d9-4a5cfc6fb148")).lastRestock(Date.valueOf("2015-12-12")).build(),
                Product.builder().id(3).sku("12345").description("Bananas").uuid(UUID.fromString("e73b5a3d-e244-4b91-ae00-8fb6bb2fa91e")).lastRestock(Date.valueOf("2014-12-12")).build(),
                Product.builder().id(4).sku("12345").description("Peaches").uuid(UUID.fromString("1a4ff82a-7899-4f6b-b46d-883040a375a7")).lastRestock(Date.valueOf("2013-12-12")).build(),
                Product.builder().id(5).sku("12345").description("Pears").uuid(UUID.fromString("b3d0ad20-f16e-47ac-bc67-a05f2695838b")).lastRestock(Date.valueOf("2012-12-12")).build(),
            };
        }

    }

}
