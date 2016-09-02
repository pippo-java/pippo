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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.ContentTypeEngine;
import ro.pippo.core.ParameterValue;
import ro.pippo.core.util.ClassUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author James Moger
 */
@MetaInfServices
public class CsvEngine implements ContentTypeEngine {

    public final static String TEXT_CSV = "text/csv";

    private final Logger log = LoggerFactory.getLogger(CsvEngine.class);

    private final static String YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";

    private boolean caseSensitiveFieldNames;
    private String datePattern = YYYYMMDDHHMMSS;
    private Character delimiter = ',';
    private Character escapeCharacter;
    private Character quoteCharacter = '\"';
    private QuoteMode quoteMode;
    private String nullString;
    private String recordSeparator = "\r\n";

    /**
     * Controls case-sensitivity when mapping CSV column names to object fields during deserialization from CSV.
     *
     * @param caseSensitiveFieldNames
     */
    public void setCaseSensitiveFieldNames(boolean caseSensitiveFieldNames) {
        this.caseSensitiveFieldNames = caseSensitiveFieldNames;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public void setEscapeCharacter(char escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    public void setQuoteCharacter(char quoteCharacter) {
        this.quoteCharacter = quoteCharacter;
    }

    public void setQuoteMode(QuoteMode quoteMode) {
        this.quoteMode = quoteMode;
    }

    public void setNullString(String nullString) {
        this.nullString = nullString;
    }

    public void setRecordSeparator(String recordSeparator) {
        this.recordSeparator = recordSeparator;
    }

    public CSVFormat getCSVFormat() {
        return CSVFormat.DEFAULT
            .withDelimiter(delimiter)
            .withRecordSeparator(recordSeparator)
            .withNullString(nullString)
            .withEscape(escapeCharacter)
            .withQuote(quoteCharacter)
            .withQuoteMode(quoteMode);
    }

    @Override
    public void init(Application application) {
    }

    @Override
    public String getContentType() {
        return TEXT_CSV;
    }

    @Override
    public String toString(Object object) {
        if (object instanceof Csv) {
            return toCsv((Csv) object);
        } else if (object.getClass().isArray() && Csv.class.isAssignableFrom(object.getClass().getComponentType())) {
            return toCsv((Csv[]) object);
        } else if (Collection.class.isAssignableFrom(object.getClass())) {
            // Collections are supported for serialization to CSV
            ArrayList<?> list = new ArrayList<>((Collection<?>) object);
            for (Object item : list) {
                if (!(item instanceof Csv)) {
                    throw new RuntimeException("All objects in the supplied collection must implement " + Csv.class.getName());
                }
            }

            return toCsv(list.toArray(new Csv[list.size()]));
        }

        throw new RuntimeException("Unexpected object type " + object.getClass().getName());
    }

    public String toCsv(Csv... records) {
        if (records != null && records.length > 0) {
            String[] header = records[0].getCsvHeader();
            StringWriter writer = new StringWriter();
            try (CSVPrinter printer = getCSVFormat().withHeader(header).print(writer)) {
                for (Csv record : records) {
                    Object [] data = record.getCsvData();
                    if (data == null || data.length == 0) {
                        log.debug("Skipping null or empty record");
                        continue;
                    }

                    for (Object column : data) {
                        printer.print(objectToString(column));
                    }
                    printer.println();
                }
            } catch (IOException e) {
                log.error("Failed to generate CSV", e);
            }

            return writer.toString();
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromString(String content, Class<T> classOfT) {
        if (!classOfT.isArray()) {
            if (Collection.class.isAssignableFrom(classOfT)) {
                // Collections are NOT supported for deserialization from CSV
                throw new RuntimeException("Collection types are not supported. Please specify an array[] type.");
            }
            throw new RuntimeException(String.format("Array[] types are required. Please specify %s[]", classOfT.getName()));
        }

        Class<?> objectType = classOfT.getComponentType();
        int currentLine = 0;
        try (CSVParser parser = new CSVParser(new StringReader(content), getCSVFormat().withHeader())) {
            Set<String> columns = parser.getHeaderMap().keySet();
            Map<String, Field> fieldMap = getFieldMap(objectType);

            Constructor<?> objectConstructor;
            try {
                objectConstructor = objectType.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("A default constructor is required for " + objectType.getName());
            }

            List objects = new ArrayList<>();
            for (CSVRecord record : parser) {
                currentLine++;

                Object o = objectConstructor.newInstance();
                for (String column : columns) {
                    Field field = fieldMap.get(caseSensitiveFieldNames ? column : column.toLowerCase());
                    String value = record.get(column);
                    Object object = objectFromString(value, field.getType());
                    field.set(o, object);
                }

                objects.add(o);
            }

            Object array = Array.newInstance(objectType, objects.size());
            for (int i = 0; i < objects.size(); i++) {
                Array.set(array, i, objects.get(i));
            }

            return (T) array;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV near line #" + currentLine, e);
        }
    }

    public String objectToString(Object object) {
        if (object == null) {
            return null;
        }

        Class<?> objectClass = object.getClass();
        if (objectClass.isEnum()) {
            return ((Enum<?>) object).name();
        } else if (object instanceof java.sql.Date) {
            return object.toString();
        } else if (object instanceof java.sql.Time) {
            return object.toString();
        } else if (object instanceof java.sql.Timestamp) {
            return object.toString();
        } else if (object instanceof Date) {
            return new SimpleDateFormat(datePattern).format((Date) object);
        }

        return object.toString();
    }

    public Object objectFromString(String value, Class<?> objectClass) throws ParseException {
        if (value == null) {
            return null;
        } else if (objectClass.isAssignableFrom(value.getClass())) {
            return value;
        }

        // re-use the infinitely useful ParameterValue class
        ParameterValue pv = new ParameterValue(value);
        if (objectClass.isEnum()) {
            return pv.toEnum((Class) objectClass);
        } else if (java.sql.Date.class.isAssignableFrom(objectClass)) {
            return pv.toSqlDate();
        } else if (java.sql.Time.class.isAssignableFrom(objectClass)) {
            return pv.toSqlTime();
        } else if (java.sql.Timestamp.class.isAssignableFrom(objectClass)) {
            return pv.toSqlTimestamp();
        } else if (Date.class.isAssignableFrom(objectClass)) {
            return pv.toDate(datePattern);
        }

        return pv.to(objectClass);
    }

    private Map<String, Field> getFieldMap(Class<?> classOfT) {
        Map<String, Field> map = new HashMap<>();
        for (Field field : ClassUtils.getAllFields(classOfT)) {
            field.setAccessible(true);
            String name;
            if (caseSensitiveFieldNames) {
                name = field.getName();
            } else {
                name = field.getName().toLowerCase();
            }
            map.put(name, field);
        }

        return map;
    }

}
