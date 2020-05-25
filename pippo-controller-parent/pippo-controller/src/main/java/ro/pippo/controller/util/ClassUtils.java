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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
        Set<Class<?>> classes = new HashSet<>();
        for (String packageName : packageNames) {
            final String packagePath = packageName.replace('.', '/');
            final String packagePrefix = packageName + '.';
            List<URL> packageUrls = getResources(packagePath);
            for (URL packageUrl : packageUrls) {
                if (packageUrl.getProtocol().equals("jar")) {
                    log.debug("Scanning jar '{}' for classes", packageUrl);
                    classes.addAll(getClassesFromJar(packageUrl, packagePrefix));
                } else {
                    log.debug("Scanning filesystem '{}' for classes (protocol = {})", packageUrl, packageUrl.getProtocol());
                    classes.addAll(getClassesFromFileSystem(packageUrl, packagePrefix));
                }
            }
        }
        return Collections.unmodifiableCollection(classes);
    }

    private static Set<Class<?>> getClassesFromJar(URL packageUrl, String packagePrefix) {
        try {
            String jar = packageUrl.toString().substring("jar:".length()).split("!")[0];
            File file = new File(new URI(jar));
            Set<Class<?>> classes = new HashSet<>();
            try (JarInputStream is = new JarInputStream(new FileInputStream(file))) {
                JarEntry entry;
                while ((entry = is.getNextJarEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                        String className = entry.getName().replace(".class", "").replace('/', '.');
                        if (className.startsWith(packagePrefix)) {
                            Class<?> aClass = ClassUtils.getClass(className);
                            classes.add(aClass);
                        }
                    }
                }
            }
            return classes;
        } catch (URISyntaxException | IOException e) {
            throw new PippoRuntimeException(e, "Failed to get classes for package '{}'", packagePrefix);
        }
    }

    private static Set<Class<?>> getClassesFromFileSystem(URL packageUrl, String packagePrefix) {
        return getClassFiles(packageUrl).stream()
            .map(filePath -> {
                filePath = filePath.replace(File.separatorChar, '.').replaceFirst(".class$", "");
                return packagePrefix + filePath.split(packagePrefix, 2)[1];
            })
            .map(className -> {
                try {
                    return ClassUtils.getClass(className);
                } catch (Exception e) {
                    log.error("Failed to find {}", className, e);
                }
                return null;
            })
            .collect(Collectors.toSet());
    }

    private static List<String> getClassFiles(URL packageUrl) {
        try (Stream<Path> walk = Files.walk(Paths.get(packageUrl.toURI()))) {
            List<String> result = walk.map(p -> p.toString())
                    .filter(p -> p.endsWith(".class"))
                    .collect(Collectors.toList());
            return result;
        } catch (IOException | URISyntaxException e) {
            throw new PippoRuntimeException(e, "Failed to get file paths for the '{}' directory", packageUrl.toString());
        }
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
