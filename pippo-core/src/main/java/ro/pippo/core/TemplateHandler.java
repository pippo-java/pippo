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
package ro.pippo.core;

import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

import java.util.Map;

/**
 * A very simple route handler that renders a template.
 * <p>
 * This handler can be useful for designs which blend server-side page
 * generation with client-side applications.
 * </p>
 *
 * @author James Moger
 */
public class TemplateHandler implements RouteHandler {

    protected final String template;
    protected final Map<String, Object> model;

    public TemplateHandler(String template) {
        this(template, null);
    }

    public TemplateHandler(String template, Map<String, Object> model) {
        this.template = template;
        this.model = model;
    }

    @Override
    public void handle(RouteContext routeContext) {
        if (model == null || model.isEmpty()) {
            routeContext.render(template);
        } else {
            routeContext.render(template, model);
        }
    }

}
