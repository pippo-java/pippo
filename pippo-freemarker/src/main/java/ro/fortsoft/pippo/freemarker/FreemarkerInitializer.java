/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.freemarker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.Initializer;

/**
 * @author Decebal Suiu
 */
public class FreemarkerInitializer implements Initializer {

    private static final Logger log = LoggerFactory.getLogger(FreemarkerInitializer.class);

    @Override
    public void init(Application application) {
//        application.setTemplateEngine(new FreemarkerTemplateEngine());
    }

    @Override
    public void destroy(Application application) {
    }

}
