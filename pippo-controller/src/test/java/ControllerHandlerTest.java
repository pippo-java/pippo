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
import org.junit.Test;
import ro.pippo.controller.Controller;
import ro.pippo.controller.DefaultControllerHandler;
import ro.pippo.core.Param;
import ro.pippo.core.ParamPattern;

import static org.junit.Assert.assertNotNull;

/**
 * @author ScienJus
 */
public class ControllerHandlerTest {

    @Test
    public void testDateParam() throws Exception {
        DefaultControllerHandler handler = new DefaultControllerHandler(DateController.class, "testDateParam");
        assertNotNull(handler.getMethod());
    }

    @Test
    public void testDateParamWithPattern() throws Exception {
        DefaultControllerHandler handler = new DefaultControllerHandler(DateController.class, "testDateParamWithPattern");
        assertNotNull(handler.getMethod());
    }

    static class DateController extends Controller {

        public void testDateParam(@Param("date") java.util.Date since) {
            System.out.println(since);
        }

        // yyyy-MM-dd HH:mm is not supported from in java.sql.Date/Time/Timestamp, just for test
        public void testDateParamWithPattern(@ParamPattern("yyyy-MM-dd HH:mm") @Param("date") java.util.Date since) {
            System.out.println(since);
        }

    }
}
