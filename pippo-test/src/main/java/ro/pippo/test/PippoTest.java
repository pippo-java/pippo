/*
 * Copyright (C) 2015 the original author or authors.
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
package ro.pippo.test;

import com.jayway.restassured.RestAssured;

/**
 * Pippo Test marries Pippo, JUnit and RestAssured to provide a convenient
 * way to perform integration tests of your Pippo application.
 * <p>
 * PippoTest will start your Pippo application on a dynamically assigned port
 * in TEST mode for execution of your unit tests.
 * </p>
 * <pre>
 * // one Pippo instance for EACH test
 * @Rule
 * public PippoRule pippoRule = new PippoRule(new PippoApplication());
 *
 * // or
 * // one Pippo instance for ALL tests
 * @ClassRule
 * public static PippoRule pippoRule = new PippoRule(new PippoApplication());
 * </pre>
 *
 * @author Decebal Suiu
 */
public abstract class PippoTest extends RestAssured {

    static {
        System.setProperty("pippo.mode", "test");
    }

}
