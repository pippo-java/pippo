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

/**
 * @author James Moger
 */
public interface Csv {

    /**
     * Return an array of column names for your CSV data.
     *
     * @return null or an array of column names
     */
    String[] getCsvHeader();

    /**
     * Return an array of objects that represent the CSV data.
     * If the returned array is null or empty, the record is skipped.
     *
     * @return null or an array of objects
     */
    Object[] getCsvData();

}
