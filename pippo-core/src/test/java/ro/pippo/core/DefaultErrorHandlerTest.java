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
package ro.pippo.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import ro.pippo.core.route.RouteContext;

/**
 * @author Tim Hinkes
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultErrorHandlerTest {
    private DefaultErrorHandler defaultErrorHandler;
    private RouteContext routeContext;

    @Before
    public void setUp() {
        Application application = Mockito.mock(Application.class);
        this.routeContext = Mockito.mock(RouteContext.class);
        this.defaultErrorHandler = new DefaultErrorHandler(application);
    }

    //Tests whether a ExceptionHandler for PippoRuntimeExceptions is called
    //if it is present. (To preserve previous behavior)
    @Test
    public void testPippoRuntimeExceptionHandlerCalledIfPResent() {
        //arrange
        ExceptionHandler pippoRuntimeExceptionExceptionHandler = Mockito.mock
            (ExceptionHandler.class);
        PippoRuntimeException pippoRuntimeException = new
            PippoRuntimeException("TestException");
        defaultErrorHandler.setExceptionHandler(PippoRuntimeException.class,
            pippoRuntimeExceptionExceptionHandler);

        //act
        defaultErrorHandler.handle(pippoRuntimeException, routeContext);

        //assert
        Mockito.verify(pippoRuntimeExceptionExceptionHandler, Mockito.times
            (1)).handle(pippoRuntimeException, routeContext);
    }

    @Test
    public void testNestedExceptionHandlerIsCalled() {
        //arrange
        ExceptionHandler testExceptionHandler = Mockito.mock
            (ExceptionHandler.class);
        TestException nestedTestException = new TestException("TestException");
        defaultErrorHandler.setExceptionHandler(TestException.class,
            testExceptionHandler);

        //act
        defaultErrorHandler.handle(new PippoRuntimeException
            (nestedTestException), routeContext);

        //assert
        Mockito.verify(testExceptionHandler, Mockito.times
            (1)).handle(nestedTestException, routeContext);
    }

    private class TestException extends Exception {
        public TestException(final String message) {
            super(message);
        }
    }

}
