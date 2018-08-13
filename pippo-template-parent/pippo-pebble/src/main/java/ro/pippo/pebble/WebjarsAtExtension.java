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
package ro.pippo.pebble;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Function;
import ro.pippo.core.route.Router;
import ro.pippo.core.route.WebjarsResourceHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Extension for handling webjar resource url generation from a Pebble template.
 *
 * @author James Moger
 */
public class WebjarsAtExtension extends AbstractExtension {

    final Function function;

    public WebjarsAtExtension(Router router) {
        this.function = new WebjarResourceFunction(router);
    }

    @Override
    public Map<String, Function> getFunctions() {
        Map<String, Function> functions = new HashMap<>();
        functions.put("webjarsAt", function);

        return functions;
    }

    class WebjarResourceFunction extends ClasspathResourceFunction<WebjarsResourceHandler> {

        protected WebjarResourceFunction(Router router) {
            super(router, WebjarsResourceHandler.class);
        }

    }

}
