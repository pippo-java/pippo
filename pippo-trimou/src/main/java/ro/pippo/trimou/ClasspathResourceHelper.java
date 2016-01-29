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
package ro.pippo.trimou;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.trimou.handlebars.BasicValueHelper;
import org.trimou.handlebars.Options;

import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.route.ClasspathResourceHandler;
import ro.pippo.core.route.Router;

/**
 * Base class for handling classpath resource url generation from a Trimou template.
 *
 * @author James Moger
 * @param <T>
 */
abstract class ClasspathResourceHelper<T extends ClasspathResourceHandler> extends BasicValueHelper {

    final Router router;
    final Class<T> resourceHandlerClass;
    final AtomicReference<String> patternRef;

    protected ClasspathResourceHelper(Router router, Class<T> resourceHandlerClass) {
        this.router = router;
        this.resourceHandlerClass = resourceHandlerClass;
        this.patternRef = new AtomicReference<>();
    }

    @Override
    public void execute(Options options) {
        if (patternRef.get() == null) {
            String pattern = router.uriPatternFor(resourceHandlerClass);
            if (pattern == null) {
                throw new PippoRuntimeException("You must register a route for {}", resourceHandlerClass.getSimpleName());
            }
            patternRef.set(pattern);
        }

        String path = (String) options.getParameters().get(0);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ClasspathResourceHandler.PATH_PARAMETER, path);
        String url = router.uriFor(patternRef.get(), parameters);
        append(options, url);
    }

}