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
import ro.pippo.core.route.PublicResourceHandler;
import ro.pippo.core.route.Router;

import java.util.HashMap;
import java.util.Map;

/**
 * Extension for handling public resource url generation from a Pebble template.
 *
 * @author James Moger
 */
public class PublicAtExtension extends AbstractExtension {

    final Function function;

    public PublicAtExtension(Router router) {
        this.function = new PublicResourceFunction(router);
    }

    @Override
    public Map<String, Function> getFunctions() {
        Map<String, Function> functions = new HashMap<>();
        functions.put("publicAt", function);

        return functions;
    }

    class PublicResourceFunction extends ClasspathResourceFunction<PublicResourceHandler> {

        protected PublicResourceFunction(Router router) {
            super(router, PublicResourceHandler.class);
        }

    }

}
