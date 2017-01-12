/*
 * Copyright (C) 2016 the original author or authors.
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
package ro.pippo.controller.util;

import ro.pippo.controller.Consumes;
import ro.pippo.controller.Interceptor;
import ro.pippo.controller.Produces;
import ro.pippo.controller.extractor.Param;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author James Moger
 */
public class ControllerUtils {

    public static List<Class<? extends RouteHandler>> collectRouteInterceptors(Method method) {
        return ClassUtils.collectNestedAnnotation(method, Interceptor.class).stream()
                .map(Interceptor::value)
                .collect(Collectors.toList());
    }

    // TODO one controllerMethod that takes annotation type as parameter
    public static List<String> getConsumes(Method method) {
        Set<String> types = new LinkedHashSet<>();
        Consumes consumes = ClassUtils.getAnnotation(method, Consumes.class);
        if (consumes != null) {
            for (String value : consumes.value()) {
                types.add(value.trim());
            }
        }

        return new ArrayList<>(types);
    }

    // TODO one controllerMethod that takes annotation type as parameter
    public static List<String> getProduces(Method method) {
        Set<String> contentTypes = new LinkedHashSet<>();
        Produces produces = ClassUtils.getAnnotation(method, Produces.class);
        if (produces != null) {
            for (String value : produces.value()) {
                contentTypes.add(value.trim());
            }
        }

        return new ArrayList<>(contentTypes);
    }

    public static Collection<String> getSuffixes(Method method) {
        Set<String> suffixes = new LinkedHashSet<>();
        for (String produces : getProduces(method)) {
            int i = produces.lastIndexOf('/') + 1;
            String type = StringUtils.removeStart(produces.substring(i).toLowerCase(), "x-");
            suffixes.add(type);
        }

        return suffixes;
    }

    // TODO
    /*
    public static Collection<Return> getReturns(Method controllerMethod) {
        Map<Integer, Return> returns = new TreeMap<>();

        if (controllerMethod.getDeclaringClass().isAnnotationPresent(Returns.class)) {
            for (Return aReturn : controllerMethod.getDeclaringClass().getAnnotation(Returns.class).value()) {
                returns.put(aReturn.code(), aReturn);
            }
        }
        if (controllerMethod.getDeclaringClass().isAnnotationPresent(Return.class)) {
            Return aReturn = controllerMethod.getDeclaringClass().getAnnotation(Return.class);
            returns.put(aReturn.code(), aReturn);
        }
        if (controllerMethod.isAnnotationPresent(Returns.class)) {
            for (Return aReturn : controllerMethod.getAnnotation(Returns.class).value()) {
                returns.put(aReturn.code(), aReturn);
            }
        }
        if (controllerMethod.isAnnotationPresent(Return.class)) {
            Return aReturn = controllerMethod.getAnnotation(Return.class);
            returns.put(aReturn.code(), aReturn);
        }

        return returns.values();
    }
    */

    /**
     * Returns the name of a parameter.
     *
     * @param parameter
     * @return the name of a parameter.
     */
    public static String getParameterName(Parameter parameter) {
        // identify parameter name and pattern from controllerMethod signature
        String methodParameterName = parameter.getName();
        if (parameter.isAnnotationPresent(Param.class)) {
            Param param = parameter.getAnnotation(Param.class);
            if (!StringUtils.isNullOrEmpty(param.value())) {
                methodParameterName = param.value();
            }
        }

        return methodParameterName;
    }

    /**
     * Removes trailing wildcards from a content type as long as the content type is not a
     * universal wildcard content type like '*' or '*\*'.
     *
     * @param contentTypes
     * @return the list of content types
     */
    public static List<String> cleanupFuzzyContentTypes(List<String> contentTypes) {
        if (contentTypes == null || contentTypes.isEmpty()) {
            return contentTypes;
        }

        List<String> types = new ArrayList<>();
        for (String contentType : contentTypes) {
            if (contentType.equals("*") || contentType.equals("*/*")) {
                types.add(contentType);
                continue;
            }
            int i = contentType.indexOf('*');
            if (i > -1) {
                types.add(contentType.substring(0, i));
            } else {
                types.add(contentType);
            }
        }

        return types;
    }

}
