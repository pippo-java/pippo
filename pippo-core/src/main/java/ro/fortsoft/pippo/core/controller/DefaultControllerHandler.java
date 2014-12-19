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

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;
import ro.fortsoft.pippo.core.util.LangUtils;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Decebal Suiu
 */
public class DefaultControllerHandler implements ControllerHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultControllerHandler.class);

    protected final Class<? extends Controller> controllerClass;
    protected final String methodName;
    protected final Method method;

    public DefaultControllerHandler(Class<? extends Controller> controllerClass, String methodName) {
        this.controllerClass = controllerClass;
        this.methodName = methodName;
        this.method = findMethod(controllerClass, methodName);
        if (method == null) {
            throw new PippoRuntimeException("Failed to find controller method '{}.{}'",
                    controllerClass.getSimpleName(), methodName);
        }
    }

    @Override
    public Class<? extends Controller> getControllerClass() {
        return controllerClass;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void handle(Request request, Response response, RouteHandlerChain chain) {
        log.debug("Invoke method '{}'", LangUtils.toString(method));
        try {
            // create the controller instance
            Controller controller = controllerClass.newInstance();
            Application.get().getControllerInstantiationListeners().onInstantiation(controller);

            // init controller
            controller.init(request, response, chain);
            Application.get().getControllerInitializationListeners().onInitialize(controller);

            // invoke action (a method from controller)
            Application.get().getControllerInvokeListeners().onInvoke(controller, method);

            Object [] args = prepareMethodArgs(request);
            method.invoke(controller, args);
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    protected Method findMethod(Class<? extends Controller> controllerClass, String name) {
        // identify first method which matches the name
        Method controllerMethod = null;
        try {
            controllerMethod = controllerClass.getMethod(methodName, new Class[] {});
        } catch (NoSuchMethodException e) {
        }

        return controllerMethod;
    }

    protected Object [] prepareMethodArgs(Request request) {
        return new Object[] {};
    }

}
