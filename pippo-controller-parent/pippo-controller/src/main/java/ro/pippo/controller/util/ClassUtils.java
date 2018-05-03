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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.PippoRuntimeException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

/**
 * Class reflection utility methods.
 *
 * @author James Moger
 */
public class ClassUtils {

    private final static Logger log = LoggerFactory.getLogger(ClassUtils.class);

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (Exception e) {
            throw new PippoRuntimeException("Failed to get class '{}'", className);
        }
    }

    /**
     * Returns the list of all classes within a package.
     *
     * @param packageNames
     * @return a collection of classes
     */
    public static Collection<Class<?>> getClasses(String... packageNames) {
        List<Class<?>> classes = new ArrayList<>();
        for (String packageName : packageNames) {
            final String packagePath = packageName.replace('.', '/');
            final String packagePrefix = packageName + '.';
            List<URL> packageUrls = getResources(packagePath);
            for (URL packageUrl : packageUrls) {
                if (packageUrl.getProtocol().equals("jar")) {
                    log.debug("Scanning jar {} for classes", packageUrl);
                    try {
                        String jar = packageUrl.toString().substring("jar:".length()).split("!")[0];
                        File file = new File(new URI(jar));
                        try (JarInputStream is = new JarInputStream(new FileInputStream(file))) {
                            JarEntry entry;
                            while ((entry = is.getNextJarEntry()) != null) {
                                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                                    String className = entry.getName().replace(".class", "").replace('/', '.');
                                    if (className.startsWith(packagePrefix)) {
                                        Class<?> aClass = getClass(className);
                                        classes.add(aClass);
                                    }
                                }
                            }
                        }
                    } catch (URISyntaxException | IOException e) {
                        throw new PippoRuntimeException("Failed to get classes for package '{}'", e, packageName);
                    }
                } else {
                    log.debug("Scanning filesystem {} for classes", packageUrl);
                    log.debug(packageUrl.getProtocol());
                    try (InputStream is = packageUrl.openStream()) {
                        Objects.requireNonNull(is, String.format("Package url %s stream is null!", packageUrl));
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                            classes.addAll(reader.lines()
                                    .filter(line -> line != null && line.endsWith(".class"))
                                    .map(line -> {
                                        String className = line.replace(".class", "").replace('/', '.');
                                        try {
                                            return getClass(packagePrefix + className);
                                        } catch (Exception e) {
                                            log.error("Failed to find {}", line, e);
                                        }

                                        return null;
                                    })
                                    .collect(Collectors.toList()));
                        }
                    } catch (IOException e) {
                        throw new PippoRuntimeException("Failed to get classes for package '{}'", e, packageName);
                    }
                }
            }
        }

        return Collections.unmodifiableCollection(classes);
    }

    /**
     * Gets all sub types in hierarchy of a given type.
     */
    public static <T> Collection<Class<? extends T>> getSubTypesOf(Class<T> type, String... packageNames) {
        List<Class<? extends T>> classes = getClasses(packageNames).stream()
                .filter(aClass -> type.isAssignableFrom(aClass))
                .map(aClass -> (Class<? extends T>) aClass)
                .collect(Collectors.toList()); // TODO: pass a Supplier to toList() to avoid map

        return Collections.unmodifiableCollection(classes);
    }

    public static Collection<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotationClass, String... packageNames) {
        List<Class<?>> classes = getClasses(packageNames).stream()
                .filter(aClass -> aClass.isAnnotationPresent(annotationClass))
                .collect(Collectors.toList());

        return Collections.unmodifiableCollection(classes);
    }

    /**
     * Extract the annotation from the controllerMethod or the declaring class.
     *
     * @param method
     * @param annotationClass
     * @param <T>
     * @return the annotation or null
     */
    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        T annotation = method.getAnnotation(annotationClass);
        if (annotation == null) {
            annotation = getAnnotation(method.getDeclaringClass(), annotationClass);
        }

        return annotation;
    }

    public static <T extends Annotation> T getAnnotation(Class<?> objectClass, Class<T> annotationClass) {
        if (objectClass == null || Object.class == objectClass) {
            return null;
        }

        T annotation = objectClass.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }

        return getAnnotation(objectClass.getSuperclass(), annotationClass);
    }

    /**
     * Returns an array containing {@code Method} objects reflecting all the
     * declared methods of the class or interface represented by this {@code
     * Class} object, including public, protected, default (package)
     * access, and private methods, <b>but excluding inherited methods</b>.
     *
     * <p>This method differs from {@link Class#getDeclaredMethods()} since it
     * does <b>not return bridge methods</b>, in other words, only the methods of
     * the class are returned. <b>If you just want the methods declared in
     * {@code clazz} use this method!</b></p>
     *
     * @param clazz Class
     *
     * @return the array of {@code Method} objects representing all the
     *          declared methods of this class
     *
     * @see Class#getDeclaredMethods()
     */
    public static List<Method> getDeclaredMethods(Class<?> clazz) {
        return Arrays
                .stream(clazz.getDeclaredMethods())
                .filter(method -> !method.isBridge())
                .collect(Collectors.toList());
    }

    public static <T extends Annotation> List<T> collectNestedAnnotation(Method method, Class<T> annotationClass) {
        List<T> list = new ArrayList<>();
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(annotationClass)) {
                T nestedAnnotation = annotation.annotationType().getAnnotation(annotationClass);
                list.add(nestedAnnotation);
            }
        }
        list.addAll(collectNestedAnnotation(method.getDeclaringClass(), annotationClass));

        return list;
    }

    public static <T extends Annotation> List<T> collectNestedAnnotation(Class<?> objectClass, Class<T> annotationClass) {
        if (objectClass == null || objectClass == Object.class) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<>();
        for (Annotation annotation : objectClass.getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(annotationClass)) {
                T nestedAnnotation = annotation.annotationType().getAnnotation(annotationClass);
                list.add(nestedAnnotation);
            }
        }
        list.addAll(collectNestedAnnotation(objectClass.getSuperclass(), annotationClass));

        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T> T executeDeclaredMethod(Object o, String methodName, Object... args) {
        try {
            Method method;
            if (args != null && args.length > 0) {
                Class<?>[] types = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) {
                    types[i] = args[i].getClass();
                }
                method = o.getClass().getDeclaredMethod(methodName, types);
                Objects.requireNonNull(method, String.format("Failed to find declared controllerMethod '%s' for args '{}' in type '%s'!",
                        methodName, types, o.getClass().getName()));
            } else {
                method = o.getClass().getDeclaredMethod(methodName);
                Objects.requireNonNull(method, String.format("Failed to find declared controllerMethod '%s' in type '%s'!",
                    methodName, o.getClass().getName()));
            }

            return (T) method.invoke(o, args);
        } catch (Exception e) {
            throw new PippoRuntimeException("Failed to execute controllerMethod '{}' on object '{}'!", e, methodName);
        }
    }

    public static List<URL> getResources(String name) {
        List<URL> list = new ArrayList<>();
        try {
            Enumeration<URL> resources = ClassUtils.class.getClassLoader().getResources(name);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                list.add(url);
            }
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }

        return list;
    }

    public static boolean isAssignable(Object value, Class<?> type) {
        if (type.isInstance(value)) {
            // inheritance
            return true;
        }
        if (boolean.class == type && value instanceof Boolean) {
            return true;
        } else if (type.isPrimitive() && value instanceof Number) {
            return true;
        }

        return false;
    }

}
