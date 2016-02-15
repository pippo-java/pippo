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
package ro.pippo.controller.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.IoUtils;
import ro.pippo.core.util.LangUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class reflection utility methods.
 *
 * @author James Moger
 */
public class ClassUtils {

    private final static Logger log = LoggerFactory.getLogger(ClassUtils.class);

    public static String buildClassName(Optional<String> packageName, String className) {
        if (packageName.isPresent()) {
            return Stream.of(packageName.get(), className).collect(Collectors.joining("."));
        } else {
            return className;
        }
    }

    public static boolean doesClassExist(Optional<String> packageName, String className) {
        return doesClassExist(buildClassName(packageName, className));
    }

    public static boolean doesClassExist(String className) {
        boolean exists = false;

        try {
            Class.forName(className, false, ClassUtils.class.getClassLoader());
            exists = true;
        } catch (ClassNotFoundException e) {
            // do nothing
        }

        return exists;
    }

    public static <T> Class<T> getClass(Optional<String> packageName, String className) {
        return getClass(buildClassName(packageName, className));
    }

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
                            JarEntry entry = null;
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

    public static Collection<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotationClass, String... packageNames) {
        List<Class<?>> classes = getClasses(packageNames).stream()
                .filter(aClass -> aClass.isAnnotationPresent(annotationClass))
                .collect(Collectors.toList());

        return Collections.unmodifiableCollection(classes);
    }

    /**
     * Extract the annotation from the method or the declaring class.
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

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotation(Parameter parameter, Class<T> annotationClass) {
        for (Annotation annotation : parameter.getAnnotations()) {
            if (annotation.annotationType() == annotationClass) {
                return (T) annotation;
            }
        }

        return null;
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

    public static <T> T newInstance(Class<T> classOfT) {
        try {
            return (T) classOfT.getConstructor().newInstance();
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    public static <T> T newInstance(Class<T> classOfT, Object... args) {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i].getClass();
        }
        try {
            Constructor<T> constructor = classOfT.getConstructor(types);
            Objects.requireNonNull(constructor, String.format("Failed to find a constructor in '%s' for types '%s'", classOfT, types));

            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    /*
    public static void setField(Object target, String name, Object value) {
        Class<?> clazz = target.getClass();
        Field field = null;
        do {
            if (clazz == null || Object.class == clazz) {
                break;
            }
            try {
                field = clazz.getDeclaredField(name);
                break;
            } catch (NoSuchFieldException e) {
                // do nothing
            }

            clazz = clazz.getSuperclass();
        } while (field == null);

        Preconditions.checkNotNull(field, "Failed to find field '%s' in graph of '%s'!", name, target.getClass().getName());

        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new PippoRuntimeException("Failed to set field '{}' in '{}'!", name, target.getClass().getName());
        }
    }
    */

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
                Objects.requireNonNull(method, String.format("Failed to find declared method '%s' for args '{}' in type '%s'!",
                        methodName, types, o.getClass().getName()));
            } else {
                method = o.getClass().getDeclaredMethod(methodName);
                Objects.requireNonNull(method, String.format("Failed to find declared method '%s' in type '%s'!",
                    methodName, o.getClass().getName()));
            }

            return (T) method.invoke(o, args);
        } catch (Exception e) {
            throw new PippoRuntimeException("Failed to execute method '{}' on object '{}'!", e, methodName);
        }
    }

    public static URL getResource(String name) {
        List<URL> resources = getResources(name);
        if (resources.isEmpty()) {
            return null;
        }

        return resources.get(0);
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

    public static Class<?> getParameterGenericType(Method method, Parameter parameter) {
        Type parameterType = parameter.getParameterizedType();
        if (!ParameterizedType.class.isAssignableFrom(parameterType.getClass())) {
            throw new PippoRuntimeException("Please specify a generic parameter type for '{}' of '{}'",
                    parameter.getType().getName(), LangUtils.toString(method));
        }

        ParameterizedType parameterizedType = (ParameterizedType) parameterType;
        try {
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            throw new PippoRuntimeException("Please specify a generic parameter type for '{}' of '{}'",
                    parameter.getType().getName(), LangUtils.toString(method));
        }
    }

    public static Class<?> getGenericType(Field field) {
        Type parameterType = field.getGenericType();
        if (!ParameterizedType.class.isAssignableFrom(parameterType.getClass())) {
            throw new PippoRuntimeException("Please specify a generic parameter type for '{}' of '{}'",
                    field.getName(), field.getDeclaringClass().getName());
        }

        ParameterizedType parameterizedType = (ParameterizedType) parameterType;
        try {
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            throw new PippoRuntimeException("Please specify a generic parameter type for '{}' of '{}'",
                    field.getName(), field.getDeclaringClass().getName());
        }
    }

    /*
    public static String loadStringResource(String resource) {
        try {
            URL url;
            if (resource.startsWith("classpath:")) {
                url = getResource(resource.substring("classpath:".length()));
            } else if (resource.startsWith("url:")) {
                url = new URL(resource.substring("url:".length()));
            } else if (resource.startsWith("file:")) {
                url = new URL(resource.substring("file:".length()));
            } else {
                url = new URL(resource);
            }

            return loadStringResource(url);
        } catch (IOException e) {
            throw new PippoRuntimeException("Failed to read String resource from '{}'", e, resource);
        }
    }
    */

    public static String loadStringResource(URL resourceUrl) {
        String content = null;
        if (resourceUrl != null) {
            try {
                content = IoUtils.toString(resourceUrl.openStream());
            } catch (IOException e) {
                log.error("Failed to read String resource from {}", resourceUrl, e);
            }
        }

        return content;
    }

}
