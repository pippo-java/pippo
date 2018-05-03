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

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Dwouglas Mhagnum
 */
public class ControllerRegistryTest {

    private ControllerRegistry controllerRegistry;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        controllerRegistry = new ControllerRegistry(new ControllerApplication());
    }

    @After
    public void after() {
        controllerRegistry = null;
    }

    @Test
    public void testWithoutPath() throws Exception {
        controllerRegistry.register(WithoutPathController.class);
        assertThat(getUriPatterns(controllerRegistry), containsInAnyOrder(WithoutPathController.expectedUriPatterns()));
    }

    @Test
    public void testWithPathWithoutValue() throws Exception {
        controllerRegistry.register(WithPathWithoutValueController.class);
        assertThat(getUriPatterns(controllerRegistry),
                containsInAnyOrder(WithPathWithoutValueController.expectedUriPatterns()));
    }

    @Test
    public void testWithPathWithSingleValue() throws Exception {
        controllerRegistry.register(WithPathWithSingleValueController.class);
        assertThat(getUriPatterns(controllerRegistry),
                containsInAnyOrder(WithPathWithSingleValueController.expectedUriPatterns()));
    }

    @Test
    public void testWithPathWithMultiValue() throws Exception {
        controllerRegistry.register(WithPathWithMultiValueController.class);
        assertThat(getUriPatterns(controllerRegistry),
                containsInAnyOrder(WithPathWithMultiValueController.expectedUriPatterns()));
    }

    @Test
    public void testRegisterPackage() throws Exception {
        controllerRegistry.register(WithoutPathController.class.getPackage());
        int expectedTotalRoutes = WithoutPathController.expectedUriPatterns().length
                + WithPathWithoutValueController.expectedUriPatterns().length
                + WithPathWithSingleValueController.expectedUriPatterns().length
                + WithPathWithMultiValueController.expectedUriPatterns().length
                + WithPathWithMultiValueAndInheritanceController.expectedUriPatterns().length;
        assertEquals(expectedTotalRoutes, controllerRegistry.getRoutes().size());
    }

    @Test
    public void testRegisterControllerClass() throws Exception {
        controllerRegistry.register(WithoutPathController.class, WithPathWithoutValueController.class,
                WithPathWithSingleValueController.class, WithPathWithMultiValueController.class);
        int expectedTotalRoutes = WithoutPathController.expectedUriPatterns().length
                + WithPathWithoutValueController.expectedUriPatterns().length
                + WithPathWithSingleValueController.expectedUriPatterns().length
                + WithPathWithMultiValueController.expectedUriPatterns().length;
        assertEquals(expectedTotalRoutes, controllerRegistry.getRoutes().size());
    }

    @Test
    public void testRegisterControllerInstance() throws Exception {
        controllerRegistry.register(new WithoutPathController(), new WithPathWithoutValueController(),
                new WithPathWithSingleValueController(), new WithPathWithMultiValueController());
        int expectedTotalRoutes = WithoutPathController.expectedUriPatterns().length
                + WithPathWithoutValueController.expectedUriPatterns().length
                + WithPathWithSingleValueController.expectedUriPatterns().length
                + WithPathWithMultiValueController.expectedUriPatterns().length;
        assertEquals(expectedTotalRoutes, controllerRegistry.getRoutes().size());
    }

    @Test
    public void testRegisterControllerWithoutRoute() throws Exception {
        controllerRegistry.register(new WithoutRouteController());
        assertEquals(0, controllerRegistry.getRoutes().size());
    }

    @Test
    public void testRegisterPackageThatDoNotExist() throws Exception {
        controllerRegistry.register("ro.pippo.controller.nonexistent.package1",
                "ro.pippo.controller.nonexistent.package2");
        assertEquals(0, controllerRegistry.getRoutes().size());
    }

    @Test
    public void testRegisterComplexControllerWithInheritance() throws Exception {
        controllerRegistry.register(WithPathWithMultiValueAndInheritanceController.class);
        assertThat(getUriPatterns(controllerRegistry),
                containsInAnyOrder(WithPathWithMultiValueAndInheritanceController.expectedUriPatterns()));
    }

    public static class WithoutPathController extends Controller {

        @GET
        public void example0() {
        }

        @GET("/example1")
        public void example1() {
        }

        // without http method
        @Named("withoutHttpMethod") // to coverage branch
        public void withoutHttpMethod() {
        }

        public static String[] expectedUriPatterns() {
            return Stream.of("/", "/example1").toArray(String[]::new);
        }

    }

    @Path
    public static class WithPathWithoutValueController extends Controller {

        @GET
        public void example0() {
        }

        @GET("/example1")
        public void example1() {
        }

        @GET("/example2/")
        public void example2() {
        }

        @GET("example3")
        public void example3() {
        }

        @GET("example4/")
        public void example4() {
        }

        @GET({ "example51/", "example52", "/example53", "/example54/" })
        public void example5() {
        }

        // without http method
        public void withoutHttpMethod() {
        }

        public static String[] expectedUriPatterns() {
            return Stream.of("/", "/example1", "/example2/", "/example3", "/example4/", "/example51/", "/example52",
                    "/example53", "/example54/").toArray(String[]::new);
        }

    }

    @Path("/")
    public static class WithPathWithSingleValueController extends Controller {

        @GET
        public void example0() {
        }

        @GET("/example1")
        public void example1() {
        }

        @GET("/example2/")
        public void example2() {
        }

        @GET("example3")
        public void example3() {
        }

        @GET("example4/")
        public void example4() {
        }

        @GET({ "example51/", "example52", "/example53", "/example54/" })
        public void example5() {
        }

        // without http method
        public void withoutHttpMethod() {
        }

        public static String[] expectedUriPatterns() {
            return Stream.of("/", "/example1", "/example2/", "/example3", "/example4/", "/example51/", "/example52",
                    "/example53", "/example54/").toArray(String[]::new);
        }

    }

    @Path({ "/", "/root1", "root2", "root3/", "/root4/", "root5/root6" })
    public static class WithPathWithMultiValueController extends Controller {

        @GET
        public void example0() {
        }

        @GET("/example1")
        public void example1() {
        }

        @GET("/example2/")
        public void example2() {
        }

        @GET("example3")
        public void example3() {
        }

        @GET("example4/")
        public void example4() {
        }

        @GET({ "example51/", "example52", "/example53", "/example54/" })
        public void example5() {
        }

        // without http method
        public void withoutHttpMethod() {
        }

        public static String[] expectedUriPatterns() {
            // @formatter:off
            return Stream.of(
                    "/", "/example1", "/example2/", "/example3", "/example4/", "/example51/", "/example52", "/example53", "/example54/",
                    "/root1", "/root1/example1", "/root1/example2/", "/root1/example3", "/root1/example4/", "/root1/example51/", "/root1/example52", "/root1/example53", "/root1/example54/",
                    "/root2", "/root2/example1", "/root2/example2/", "/root2/example3", "/root2/example4/", "/root2/example51/", "/root2/example52", "/root2/example53", "/root2/example54/",
                    "/root3/", "/root3/example1", "/root3/example2/", "/root3/example3", "/root3/example4/", "/root3/example51/", "/root3/example52", "/root3/example53", "/root3/example54/",
                    "/root4/", "/root4/example1", "/root4/example2/", "/root4/example3", "/root4/example4/", "/root4/example51/", "/root4/example52", "/root4/example53", "/root4/example54/",
                    "/root5/root6", "/root5/root6/example1", "/root5/root6/example2/", "/root5/root6/example3", "/root5/root6/example4/", "/root5/root6/example51/", "/root5/root6/example52", "/root5/root6/example53", "/root5/root6/example54/"
                ).toArray(String[]::new);
            // @formatter:on
        }

    }

    @Path({ "/", "/root7" })
    public static class WithPathWithMultiValueAndInheritanceController extends ControllerAbstract {

        @GET
        public void example0() {
        }

        @GET({ "example11/", "example12", "/example13", "/example14/" })
        public void example1() {
        }

        // without http method
        public void withoutHttpMethod() {
        }

        public static String[] expectedUriPatterns() {
            // @formatter:off
            return Stream.of(
                    "/ControllerAbstract/", "/ControllerAbstract/example11/", "/ControllerAbstract/example12", "/ControllerAbstract/example13", "/ControllerAbstract/example14/",
                    "/ControllerAbstract/root7", "/ControllerAbstract/root7/example11/", "/ControllerAbstract/root7/example12", "/ControllerAbstract/root7/example13", "/ControllerAbstract/root7/example14/"
                ).toArray(String[]::new);
            // @formatter:on
        }

    }

    @Path("/ControllerAbstract")
    public static abstract class ControllerAbstract extends Controller {

    }

    @Path("/WithoutRouteController/")
    public static class WithoutRouteController extends Controller {

    }

    // for the test that scans the package
    public static class ArbitraryClass {

        @GET("/example1")
        public void example1() {
        }

    }

    private List<String> getUriPatterns(ControllerRegistry controllerRegistry) {
        return controllerRegistry.getRoutes().stream().map(r -> r.getUriPattern()).collect(Collectors.toList());
    }

}
