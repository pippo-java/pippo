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
package ro.pippo.freemarker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.route.ClasspathResourceHandler;
import ro.pippo.core.route.Router;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;

/**
 * Base class for handling classpath resource url generation from a Freemarker template.
 *
 * @author James Moger
 * @param <T>
 */
abstract class ClasspathResourceMethod<T extends ClasspathResourceHandler> implements TemplateMethodModelEx {

    public final Logger log = LoggerFactory.getLogger(getClass());

    final Router router;
    final Class<T> resourceHandlerClass;
    final AtomicReference<String> urlPattern;

    public ClasspathResourceMethod(Router router, Class<T> resourceHandlerClass) {
        this.router = router;
        this.resourceHandlerClass = resourceHandlerClass;
        this.urlPattern = new AtomicReference<>(null);
    }

    @Override
    public TemplateModel exec(List args) {
        if (urlPattern.get() == null) {
            String pattern = router.uriPatternFor(resourceHandlerClass);
            if (pattern == null) {
                throw new PippoRuntimeException("You must register a route for {}",
                        resourceHandlerClass.getSimpleName());
            }

            urlPattern.set(pattern);
        }

        String path = args.get(0).toString();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ClasspathResourceHandler.PATH_PARAMETER, path);
        String url = router.uriFor(urlPattern.get(), parameters);

        return new SimpleScalar(url);
    }

}
