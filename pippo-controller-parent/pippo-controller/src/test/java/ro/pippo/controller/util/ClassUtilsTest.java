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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.tools.JavaFileObject;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.testing.compile.JavaFileObjects;

import ro.pippo.DynamicJar;
import ro.pippo.core.PippoRuntimeException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {ClassUtils.class})
@PowerMockIgnore({
    "com.sun.tools.*",
    "javax.tools.*",
})
public class ClassUtilsTest {

    // @ClassRule
    // public static TemporaryFolder tmpFolder = new TemporaryFolder(); // don't work due to PowerMockRunner

    private static Path tmpFolder;
    private static DynamicJar dynamicJar;

    @BeforeClass
    public static void setUp() throws IOException {
        tmpFolder = Files.createTempDirectory(ClassUtilsTest.class.getSimpleName());
        dynamicJar = buildClassUtilsTestJar();
    }

    @AfterClass
    public static void tearDown() {
        tmpFolder.toFile().delete();
    }

    @Test
    public void test_getClassesFromFileSystem_withSubpackage() throws MalformedURLException {
        // Given (Input): Test preparation like creating data or configure mocks
        PowerMockito.spy(ClassUtils.class);

        final String packageString = "ro.pippo.controller.util.data";
        final String packageStringMod = packageString.replace('.', '/');

        Answer<Class<?>> answer = invocation -> getClassMock(
                (String) invocation.getArguments()[0],
                new URLClassLoader(new URL[] { dynamicJar.folder() })
            );

        PowerMockito.doAnswer(answer).when(ClassUtils.class);
        ClassUtils.getClass(Mockito.anyString()); // This line must be immediately below the line above. This is ugly!

        List<URL> urls = Collections.singletonList(dynamicJar.baseDirPath().resolve(packageStringMod).toUri().toURL());
        PowerMockito.when(ClassUtils.getResources(packageStringMod)).thenReturn(urls);

        // When (Action): Call the method or action that you like to test
        Collection<Class<?>> classes = ClassUtils.getClasses(packageString);

        // Then (Output): Execute assertions to verify the correct output or behavior of the action
        assertEquals(5, classes.size());
    }

    // aqui
    @Test
    public void test_getClassesFromFileSystem_withoutSubpackage() throws MalformedURLException {
        // Given (Input): Test preparation like creating data or configure mocks
        PowerMockito.spy(ClassUtils.class);

        final String packageString = "ro.pippo.controller.util.data.package2";
        final String packageStringMod = packageString.replace('.', '/');

        Answer<Class<?>> answer = invocation -> getClassMock(
                (String) invocation.getArguments()[0],
                new URLClassLoader(new URL[] { dynamicJar.folder() })
            );

        PowerMockito.doAnswer(answer).when(ClassUtils.class);
        ClassUtils.getClass(Mockito.anyString()); // This line must be immediately below the line above. This is ugly!

        List<URL> urls = Collections.singletonList(dynamicJar.baseDirPath().resolve(packageStringMod).toUri().toURL());
        PowerMockito.when(ClassUtils.getResources(packageStringMod)).thenReturn(urls);

        // When (Action): Call the method or action that you like to test
        Collection<Class<?>> classes = ClassUtils.getClasses(packageString);

        // Then (Output): Execute assertions to verify the correct output or behavior of the action
        assertEquals(2, classes.size());
    }

    @Test
    public void test_getClassesFromJar_withSubpackage() throws IOException {
        // Given (Input): Test preparation like creating data or configure mocks
        PowerMockito.spy(ClassUtils.class);

        final String packageString = "ro.pippo.controller.util.data";
        final String packageStringMod = packageString.replace('.', '/');

        Answer<Class<?>> answer = invocation -> getClassMock(
                (String) invocation.getArguments()[0],
                new URLClassLoader(new URL[] { dynamicJar.url() })
            );

        PowerMockito.doAnswer(answer).when(ClassUtils.class);
        ClassUtils.getClass(Mockito.anyString()); // This line must be immediately below the line above. This is ugly!

        List<URL> urls = Collections.singletonList(new URL("jar:" + dynamicJar.url() + "!/" + packageString));
        PowerMockito.when(ClassUtils.getResources(packageStringMod)).thenReturn(urls);

        // When (Action): Call the method or action that you like to test
        Collection<Class<?>> classes = ClassUtils.getClasses(packageString);

        // Then (Output): Execute assertions to verify the correct output or behavior of the action
        assertEquals(5, classes.size());
    }

    @Test
    public void test_getClassesFromJar_withoutSubpackage() throws MalformedURLException {
        // Given (Input): Test preparation like creating data or configure mocks
        PowerMockito.spy(ClassUtils.class);

        final String packageString = "ro.pippo.controller.util.data.package2";
        final String packageStringMod = packageString.replace('.', '/');

        Answer<Class<?>> answer = invocation -> getClassMock(
                (String) invocation.getArguments()[0],
                new URLClassLoader(new URL[] { dynamicJar.url() })
            );

        PowerMockito.doAnswer(answer).when(ClassUtils.class);
        ClassUtils.getClass(Mockito.anyString()); // This line must be immediately below the line above. This is ugly!

        List<URL> urls = Collections.singletonList(new URL("jar:" + dynamicJar.url() + "!/" + packageString));
        PowerMockito.when(ClassUtils.getResources(packageStringMod)).thenReturn(urls);

        // When (Action): Call the method or action that you like to test
        Collection<Class<?>> classes = ClassUtils.getClasses(packageString);

        // Then (Output): Execute assertions to verify the correct output or behavior of the action
        assertEquals(2, classes.size());
    }

    private <T> Class<T> getClassMock(String className, ClassLoader classLoader) {
        try {
            return (Class<T>) Class.forName(className, true, classLoader);
        } catch (Exception e) {
            throw new PippoRuntimeException("Failed to get class '{}'", className);
        }
    }

    private static DynamicJar buildClassUtilsTestJar() throws IOException {
        JavaFileObject class1 = JavaFileObjects.forSourceLines("ro.pippo.controller.util.data.Class1",
            "package ro.pippo.controller.util.data;",
            "public class Class1 { }"
        );

        JavaFileObject class2 = JavaFileObjects.forSourceLines("ro.pippo.controller.util.data.Class2",
            "package ro.pippo.controller.util.data;",
            "public class Class2 { }"
        );

        JavaFileObject class3 = JavaFileObjects.forSourceLines("ro.pippo.controller.util.data.Class3",
            "package ro.pippo.controller.util.data;",
            "public class Class3 { }"
        );

        JavaFileObject class4 = JavaFileObjects.forSourceLines("ro.pippo.controller.util.data.package2.Class4",
            "package ro.pippo.controller.util.data.package2;",
            "public class Class4 { }"
        );

        JavaFileObject class5 = JavaFileObjects.forSourceLines("ro.pippo.controller.util.data.package2.Class5",
            "package ro.pippo.controller.util.data.package2;",
            "public class Class5 { }"
        );

        DynamicJar dynamicJar =
            new DynamicJar.Builder(tmpFolder.resolve("ClassUtilsTest.jar"))
                .clazz(class1)
                .clazz(class2)
                .clazz(class3)
                .clazz(class4)
                .clazz(class5)
                .extract()
                .build();

        return dynamicJar;
    }

}
