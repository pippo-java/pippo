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
package ro.fortsoft.pippo.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;

import java.lang.reflect.Method;

/**
 * @author Decebal Suiu
 */
public class ControllerHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(ControllerHandler.class);

    private final Class<? extends Controller> controllerClass;
    private final String methodName;
    private final Method method;

    public ControllerHandler(Class<? extends Controller> controllerClass, String methodName) {
        this.controllerClass = controllerClass;
        this.methodName = methodName;
        try {
            this.method = controllerClass.getMethod(methodName, new Class[] {});
        } catch (NoSuchMethodException e) {
            throw new PippoRuntimeException("Failed to find controller method '{}.{}'", e,
                    controllerClass.getSimpleName(), methodName);
        }
    }

    public Class<? extends Controller> getControllerClass() {
        return controllerClass;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public void handle(Request request, Response response, RouteHandlerChain chain) {
        log.debug("Invoke method '{}' from '{}'", methodName, controllerClass.getName());
        try {
            // create the controller instance
            Controller controller = controllerClass.newInstance();
            Application.get().getControllerInstantiationListeners().onInstantiation(controller);

            // init controller
            controller.init(request, response, chain);
            Application.get().getControllerInitializationListeners().onInitialize(controller);

            // invoke action (a method from controller)
            Application.get().getControllerInvokeListeners().onInvoke(controller, method);
            method.invoke(controller, new Object[] {});
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

}
