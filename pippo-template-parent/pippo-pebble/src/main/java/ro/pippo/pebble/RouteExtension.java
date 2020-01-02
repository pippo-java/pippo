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
import ro.pippo.core.route.Router;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension that creates an url for a route, from a {@code nameOrUriPattern} and {@code parameters}.
 * You can use it in pebble template like:
 *
 * {@code
 * <a href="{{ route('quote', {'symbol': stock.symbol }) }}">{{ stock.symbol }}</a>
 * }
 *
 * where {@code quote} is a route name defined like:
 *
 * {@code
 * GET("/quote", routeContext -> {
 *     ParameterValue symbol = routeContext.getParameter("symbol");
 *     if (symbol.isEmpty()) {
 *         routeContext.getResponse().badRequest().send("Specify a symbol as parameter");
 *     } else {
 *         Stock stock = dataStore.select(Stock.class).where(Stock.SYMBOL.eq(symbol.toString())).get().first();
 *         List<Quote> quotes = dataStore.select(Quote.class).where(Quote.SYMBOL.eq(stock.symbol)).get().toList();
 *
 *         Map<String, Object> model = new HashMap<>();
 *         model.put("stock", stock);
 *         model.put("quotes", quotes);
 *
 *         routeContext.render("quote", model);
 *     }
 * }).named("quote");
 * }
 *
 * @author Decebal Suiu
 */
public class RouteExtension extends AbstractExtension {

    private Router router;

    public RouteExtension(Router router) {
        this.router = router;
    }

    @Override
    public Map<String, Function> getFunctions() {
        Map<String, Function> functions = new HashMap<>();
        functions.put("route", new RouteFunction(router));

        return functions;
    }

    class RouteFunction implements Function {

        private Router router;

        public RouteFunction(Router router) {
            this.router = router;
        }

        @Override
        public List<String> getArgumentNames() {
            List<String> names = new ArrayList<>();
            names.add("nameOrUriPattern");
            names.add("parameters");

            return names;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            String nameOrUriPattern = (String) args.get("nameOrUriPattern");
            Map<String, Object> parameters = (Map<String, Object>) args.get("parameters");
            if (parameters == null) {
                parameters = Collections.emptyMap();
            }

            return router.uriFor(nameOrUriPattern, parameters);
        }

    }

}
