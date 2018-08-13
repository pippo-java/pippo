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
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AngularJSExtension extends AbstractExtension {

    @Override
    public Map<String, Function> getFunctions() {
        Map<String, Function> functions = new HashMap<>();
        functions.put("ng", new NgFunction());

        return functions;
    }

    class NgFunction implements Function {

        @Override
        public List<String> getArgumentNames() {
            List<String> names = new ArrayList<>();
            names.add("content");

            return names;
        }

        @Override
        public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            String content = (String) args.get("content");
            return "{{ " + content + " }}";
        }

    }

}
