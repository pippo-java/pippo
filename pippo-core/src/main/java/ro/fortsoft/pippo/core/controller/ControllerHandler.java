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
import ro.fortsoft.pippo.core.Param;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;
import ro.fortsoft.pippo.core.util.LangUtils;
import ro.fortsoft.pippo.core.util.RequestValue;
import ro.fortsoft.pippo.core.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Decebal Suiu
 */
public class ControllerHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(ControllerHandler.class);

    protected final Class<? extends Controller> controllerClass;
    protected final String methodName;
    protected final Method method;
    protected String[] parameterNames;

    public ControllerHandler(Class<? extends Controller> controllerClass, String methodName) {
        this.controllerClass = controllerClass;
        this.methodName = methodName;
        this.method = findMethod(controllerClass, methodName);
        if (method == null) {
            throw new PippoRuntimeException("Failed to find controller method '{}.{}'",
                    controllerClass.getSimpleName(), methodName);
        }
    }

    public Class<? extends Controller> getControllerClass() {
        return controllerClass;
    }

    public String getMethodName() {
        return methodName;
    }

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

            Object[] args = prepareMethodArgs(request);
            method.invoke(controller, args);
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    protected Method findMethod(Class<? extends Controller> controllerClass, String name) {
        // identify first method which matches the name
        Method controllerMethod = null;
        for (Method method : controllerClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                if (controllerMethod == null) {
                    controllerMethod = method;
                    Class<?>[] types = method.getParameterTypes();
                    if (types.length == 0) {
                        // no mapped parameters
                        continue;
                    }

                    // mapped parameters
                    parameterNames = new String[types.length];
                    for (int i = 0; i < types.length; i++) {
                        // confirm parameter type is supported
                        Class<?> type = types[i];
                        RequestValue testValue = new RequestValue();
                        testValue.to(type);

                        // confirm parameter is named
                        String parameterName = getParameterName(controllerMethod, i);
                        if (StringUtils.isNullOrEmpty(parameterName)) {
                            throw new PippoRuntimeException(
                                    "Controller method '{}.{}' parameter #{} of type '{}' does not specify a name!",
                                    controllerClass.getSimpleName(), methodName, type.getSimpleName());
                        }

                        parameterNames[i] = parameterName;
                    }

                } else {
                    throw new PippoRuntimeException(
                            "Found overloaded controller method '{}.{}'. Method names must be unique!",
                            controllerClass.getSimpleName(), methodName);
                }
            }
        }

        return controllerMethod;
    }

    protected Object[] prepareMethodArgs(Request request) {
        Class<?>[] types = method.getParameterTypes();

        if (types.length == 0) {
            return new Object[] {};
        }

        Object[] args = new Object[types.length];
        for (int i = 0; i < args.length; i++) {
            Class<?> type = types[i];
            String name = parameterNames[i];
            RequestValue requestValue = request.getParameter(name);
            args[i] = requestValue.to(type);
        }
        return args;
    }

    protected String getParameterName(Method method, int i) {
        Annotation[] annotations = method.getParameterAnnotations()[i];
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == Param.class) {
                Param parameter = (Param) annotation;
                return parameter.value();
            }
        }
        return null;
    }
}
