/*
 * Copyright (C) 2015 the original author or authors.
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
package ro.pippo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.controller.util.ClassUtils;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.util.LangUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for identifying annotated controllers.
 * It's used internally by {@link ControllerRegistrar}.
 *
 * @author James Moger
 */
class ControllerScanner {

    private static final Logger log = LoggerFactory.getLogger(ControllerScanner.class);

    protected final PippoSettings settings;

    protected final Set<Class<? extends Annotation>> httpMethodAnnotationClasses = new HashSet<>(Arrays.asList(
        DELETE.class, GET.class, HEAD.class, OPTIONS.class, PATCH.class, POST.class, PUT.class));

    protected ControllerScanner(PippoSettings settings) {
        this.settings = settings;
    }

    /**
     * Discover Controller classes.
     *
     * @param packageNames
     * @return controller classes
     */
    protected Collection<Class<?>> discoverClasses(String... packageNames) {
        log.debug("Discovering annotated controller in package(s) '{}'", Arrays.toString(packageNames));
        return ClassUtils.getAnnotatedClasses(Path.class, packageNames);
    }

    /**
     * Discover Route methods.
     *
     * @param classes
     * @return discovered methods
     */
    protected Map<Method, Class<? extends Annotation>> discoverMethods(Collection<Class<?>> classes) {
        // collect the allowed annotated methods
        Map<Method, Class<? extends Annotation>> discoveredMethods = new LinkedHashMap<>();

        // discover all annotated controllers and methods
        for (Class<?> controllerClass : classes) {
            for (Method method : controllerClass.getDeclaredMethods()) {
                // TODO resolve
//                if (RequireUtil.allowMethod(settings, method)) {
                    for (Annotation annotation : method.getAnnotations()) {
                        Class<? extends Annotation> annotationClass = annotation.annotationType();
                        if (httpMethodAnnotationClasses.contains(annotationClass)) {
                            discoveredMethods.put(method, annotationClass);
                            break;
                        }
                    }
//                }
            }
        }

        return discoveredMethods;
    }

    /**
     * Sort the methods by their preferred order, if specified.
     *
     * @param methods
     * @return a sorted list of methods
     */
    protected Collection<Method> sortMethods(Collection<Method> methods) {
        List<Method> list = new ArrayList<>(methods);
        Collections.sort(list, (m1, m2) -> {
            int o1 = Integer.MAX_VALUE;
            Order order1 = ClassUtils.getAnnotation(m1, Order.class);
            if (order1 != null) {
                o1 = order1.value();
            }

            int o2 = Integer.MAX_VALUE;
            Order order2 = ClassUtils.getAnnotation(m2, Order.class);
            if (order2 != null) {
                o2 = order2.value();
            }

            if (o1 == o2) {
                // same or unsorted, compare controller+method
                String s1 = LangUtils.toString(m1);
                String s2 = LangUtils.toString(m2);

                return s1.compareTo(s2);
            }

            return (o1 < o2) ? -1 : 1;
        });

        return list;
    }

    /**
     * Recursively builds the paths for the controller class.
     *
     * @param controllerClass
     * @return the paths for the controller
     */
    protected Set<String> collectPaths(Class<?> controllerClass) {
        Set<String> parentPaths = Collections.emptySet();
        if (controllerClass.getSuperclass() != null) {
            parentPaths = collectPaths(controllerClass.getSuperclass());
        }

        Set<String> paths = new LinkedHashSet<>();
        Path controllerPath = controllerClass.getAnnotation(Path.class);

        if (controllerPath != null && controllerPath.value().length > 0) {
            if (parentPaths.isEmpty()) {
                // add all controller paths
                paths.addAll(Arrays.asList(controllerPath.value()));
            } else {
                // create controller paths based on the parent paths
                for (String parentPath : parentPaths) {
                    for (String path : controllerPath.value()) {
                        paths.add(parentPath + path);
                    }
                }
            }
        } else {
            // add all parent paths
            paths.addAll(parentPaths);
        }

        return paths;
    }

}
