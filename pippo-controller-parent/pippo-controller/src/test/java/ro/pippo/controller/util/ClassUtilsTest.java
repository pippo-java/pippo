package ro.pippo.controller.util;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.ClasspathUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {ClassUtils.class})
public class ClassUtilsTest {

    @Test
    public void test_getClassesFromFileSystem_withSubpackage() {
        Collection<Class<?>> classes = ClassUtils.getClasses("ro.pippo.controller.util.data");
        assertEquals(6, classes.size());
    }

    @Test
    public void test_getClassesFromFileSystem_withoutSubpackage() {
        Collection<Class<?>> classes = ClassUtils.getClasses("ro.pippo.controller.util.data.package2");
        assertEquals(3, classes.size());
    }

    @Test
    public void test_getClassesFromJar_withSubpackage() throws MalformedURLException {
        PowerMockito.spy(ClassUtils.class);

        final String packageString = "ro.pippo.controller.util.data";

        URL jarUrl = ClasspathUtils.locateOnClasspath("ClassUtilsTest.jar");

        Answer<Class<?>> answer = invocation -> getClassMock(
                (String) invocation.getArguments()[0],
                new URLClassLoader(new URL[] { jarUrl })
            );

        PowerMockito.doAnswer(answer).when(ClassUtils.class);
        ClassUtils.getClass(Mockito.anyString()); // This line must be immediately below the line above. This is ugly!

        List<URL> urls = Collections.singletonList(new URL("jar:" + jarUrl.toString() + "!/" + packageString));
        PowerMockito.when(ClassUtils.getResources(packageString.replace('.', '/'))).thenReturn(urls);

        Collection<Class<?>> classes = ClassUtils.getClasses(packageString);
        assertEquals(8, classes.size());
    }

    @Test
    public void test_getClassesFromJar_withoutSubpackage() throws MalformedURLException {
        PowerMockito.spy(ClassUtils.class);

        final String packageString = "ro.pippo.controller.util.data.package2";

        URL jarUrl = ClasspathUtils.locateOnClasspath("ClassUtilsTest.jar");

        Answer<Class<?>> answer = invocation -> getClassMock(
                (String) invocation.getArguments()[0],
                new URLClassLoader(new URL[] { jarUrl })
                );

        PowerMockito.doAnswer(answer).when(ClassUtils.class);
        ClassUtils.getClass(Mockito.anyString()); // This line must be immediately below the line above. This is ugly!

        List<URL> urls = Collections.singletonList(new URL("jar:" + jarUrl.toString() + "!/" + packageString));
        PowerMockito.when(ClassUtils.getResources(packageString.replace('.', '/'))).thenReturn(urls);

        Collection<Class<?>> classes = ClassUtils.getClasses(packageString);
        assertEquals(2, classes.size());
    }

    private <T> Class<T> getClassMock(String className, ClassLoader classLoader) {
        try {
            return (Class<T>) Class.forName(className, true, classLoader);
        } catch (Exception e) {
            throw new PippoRuntimeException("Failed to get class '{}'", className);
        }
    }

}
