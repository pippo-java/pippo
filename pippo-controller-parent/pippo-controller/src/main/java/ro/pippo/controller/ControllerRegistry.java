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
package ro.pippo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.controller.util.ClassUtils;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.util.LangUtils;
import ro.pippo.core.util.StringUtils;

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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Register annotated controller routes.
 *
 * @author Decebal Suiu
 * @author James Moger
 */
public class ControllerRegistry {

    private static final Logger log = LoggerFactory.getLogger(ControllerRegistry.class);

    private final Set<Class<? extends Annotation>> httpMethodAnnotationClasses = new HashSet<>(Arrays.asList(
        DELETE.class, GET.class, HEAD.class, OPTIONS.class, PATCH.class, POST.class, PUT.class));

    private final ControllerApplication application;
    private final List<Route> routes;

    public ControllerRegistry(ControllerApplication application) {
        this.application = application;

        routes = new ArrayList<>();
    }

    /**
     * Register all controller methods in the specified packages.
     *
     * @param packages
     */
    public void register(Package... packages) {
        List<String> packageNames = Arrays.stream(packages)
            .map(Package::getName)
            .collect(Collectors.toList());

        register(packageNames.toArray(new String[packageNames.size()]));
    }

    /**
     * Register all controller methods in the specified package names.
     *
     * @param packageNames
     */
    public void register(String... packageNames) {
        Collection<Class<? extends Controller>> classes = getControllerClasses(packageNames);
        if (classes.isEmpty()) {
            log.warn("No annotated controllers found in package(s) '{}'", Arrays.toString(packageNames));
            return;
        }

        log.debug("Found {} controller classes in {} package(s)", classes.size(), packageNames.length);

        for (Class<? extends Controller> controllerClass : classes) {
            register(controllerClass);
        }
    }

    /**
     * Register all controller methods in the specified controller classes.
     *
     * @param controllerClasses
     */
    public void register(Class<? extends Controller>... controllerClasses) {
        for (Class<? extends Controller> controllerClass : controllerClasses) {
            register(controllerClass);
        }
    }

    /**
     * Register all controller methods in the specified controllers.
     *
     * @param controllers
     */
    public void register(Controller... controllers) {
        for (Controller controller : controllers) {
            register(controller);
        }
    }

    /**
     * Return the collected routes.
     *
     * @return the routes
     */
    public List<Route> getRoutes() {
        return routes;
    }

    private void register(Class<? extends Controller> controllerClass) {
        Map<Method, Class<? extends Annotation>> controllerMethods = getControllerMethods(controllerClass);
        if (controllerMethods.isEmpty()) {
            // if we are using this registry we expect to discover controller routes!
            log.warn("No annotated controller methods found in class '{}'", controllerClass);
            return;
        }

        log.debug("Found {} annotated controller method(s)", controllerMethods.size());

        registerControllerMethods(controllerMethods, null);

        log.debug("Added {} annotated routes from '{}'", routes.size(), controllerClass);
    }

    public void register(Controller controller) {
        Class<? extends Controller> controllerClass = controller.getClass();
        Map<Method, Class<? extends Annotation>> controllerMethods = getControllerMethods(controllerClass);
        if (controllerMethods.isEmpty()) {
            // if we are using this registry we expect to discover controller routes!
            log.warn("No annotated controller methods found in class '{}'", controllerClass);
            return;
        }

        registerControllerMethods(controllerMethods, controller);

        log.debug("Found {} annotated controller method(s)", controllerMethods.size());
    }

    /**
     * Register the controller methods as routes.
     *
     * @param controllerMethods
     * @param controller
     */
    private void registerControllerMethods(Map<Method, Class<? extends Annotation>> controllerMethods, Controller controller) {
        List<Route> controllerRoutes = createControllerRoutes(controllerMethods);
        for (Route controllerRoute : controllerRoutes) {
            if (controller != null) {
                ((ControllerHandler) controllerRoute.getRouteHandler()).setController(controller);
                controllerRoute.bind("__controller", controller);
            }
        }

        this.routes.addAll(controllerRoutes);
    }

    /**
     * Create controller routes from controller methods.
     *
     * @param controllerMethods
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Route> createControllerRoutes(Map<Method, Class<? extends Annotation>> controllerMethods) {
        List<Route> routes = new ArrayList<>();

        Class<? extends Controller> controllerClass = (Class<? extends Controller>) controllerMethods.keySet().iterator().next().getDeclaringClass();
        Set<String> controllerPaths = getControllerPaths(controllerClass);
        Collection<Method> methods = sortControllerMethods(controllerMethods.keySet());
        for (Method method : methods) {
            Class<? extends Annotation> httpMethodAnnotationClass = controllerMethods.get(method);
            Annotation httpMethodAnnotation = method.getAnnotation(httpMethodAnnotationClass);

            String httpMethod = httpMethodAnnotation.annotationType().getAnnotation(HttpMethod.class).value();
            String[] methodPaths = ClassUtils.executeDeclaredMethod(httpMethodAnnotation, "value");

            if (controllerPaths.isEmpty()) {
                // add an empty string to allow controllerPaths iteration
                controllerPaths.add("");
            }

            for (String controllerPath : controllerPaths) {
                if (methodPaths.length == 0) {
                    // controllerMethod does not specify a path, inherit from controller
                    String fullPath = StringUtils.addStart(controllerPath, "/");

                    // create the route handler
                    RouteHandler handler = new ControllerHandler(application, method);

                    // create the route
                    Route route = new Route(httpMethod, fullPath, handler)
                        .bind("__controllerClass", controllerClass)
                        .bind("__controllerMethod", method);

                    // add the route to the list of routes
                    routes.add(route);
                } else {
                    // controllerMethod specifies one or more paths, concatenate with controller paths
                    for (String methodPath : methodPaths) {
                        String path = Stream.of(StringUtils.removeEnd(controllerPath, "/"), StringUtils.removeStart(methodPath, "/"))
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining("/"));
                        String fullPath = StringUtils.addStart(path, "/");

                        // create the route handler
                        RouteHandler handler = new ControllerHandler(application, method);

                        // create the route
                        Route route = new Route(httpMethod, fullPath, handler)
                            .bind("__controllerClass", controllerClass)
                            .bind("__controllerMethod", method);

                        // add the route to the list of routes
                        routes.add(route);
                    }
                }
            }
        }

        return routes;
    }

    /**
     * Discover Controller classes.
     *
     * @param packageNames
     * @return controller classes
     */
    private Collection<Class<? extends Controller>> getControllerClasses(String... packageNames) {
        log.debug("Discovering annotated controller in package(s) '{}'", Arrays.toString(packageNames));
        return ClassUtils.getSubTypesOf(Controller.class, packageNames);
    }

    /**
     * Discover Route methods.
     *
     * @param controllerClass
     * @return discovered methods
     */
    private Map<Method, Class<? extends Annotation>> getControllerMethods(Class<? extends Controller> controllerClass) {
        // collect the annotated methods
        Map<Method, Class<? extends Annotation>> controllerMethods = new LinkedHashMap<>();

        // discover all annotated controllers methods
        for (Method method : ClassUtils.getDeclaredMethods(controllerClass)) {
            for (Annotation annotation : method.getAnnotations()) {
                Class<? extends Annotation> annotationClass = annotation.annotationType();
                if (httpMethodAnnotationClasses.contains(annotationClass)) {
                    controllerMethods.put(method, annotationClass);
                    break;
                }
            }
        }

        return controllerMethods;
    }

    /**
     * Recursively builds the paths for the controller class.
     *
     * @param controllerClass
     * @return the paths for the controller
     */
    private Set<String> getControllerPaths(Class<?> controllerClass) {
        Set<String> parentPaths = Collections.emptySet();
        if (controllerClass.getSuperclass() != null) {
            parentPaths = getControllerPaths(controllerClass.getSuperclass());
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
                        paths.add(StringUtils.removeEnd(parentPath, "/") + "/" + StringUtils.removeStart(path, "/"));
                    }
                }
            }
        } else {
            // add all parent paths
            paths.addAll(parentPaths);
        }

        return paths;
    }

    /**
     * Sort the controller's methods by their preferred order, if specified.
     *
     * @param controllerMethods
     * @return a sorted list of methods
     */
    private Collection<Method> sortControllerMethods(Set<Method> controllerMethods) {
        List<Method> list = new ArrayList<>(controllerMethods);
        list.sort((m1, m2) -> {
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
                // same or unsorted, compare controller+controllerMethod
                String s1 = LangUtils.toString(m1);
                String s2 = LangUtils.toString(m2);

                return s1.compareTo(s2);
            }

            return (o1 < o2) ? -1 : 1;
        });

        return list;
    }

}
