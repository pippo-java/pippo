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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.util.StringValue;

/**
 * @author James Moger
 */
public class ParameterizedControllerHandler extends DefaultControllerHandler {

    private final Logger log = LoggerFactory.getLogger(ParameterizedControllerHandler.class);

    public ParameterizedControllerHandler(Class<? extends Controller> controllerClass, String methodName) {
        super(controllerClass, methodName);
    }

    @Override
    protected Method findMethod(Class<? extends Controller> controllerClass, String name) {
        // identify first method which matches the name
        Method controllerMethod = null;
        for (Method method : controllerClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                if (controllerMethod == null) {
                    controllerMethod = method;
                    Parameter[] parameters = method.getParameters();
                    for (Parameter parameter : parameters) {
                        if (!parameter.isNamePresent()) {
                            throw new PippoRuntimeException("Please re-compile '{}' with parameter metadata included!",
                                    controllerClass.getSimpleName());
                        }
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

    @Override
    protected Object[] prepareMethodArgs(Request request) {
        Class<?>[] types = method.getParameterTypes();

        if (types.length == 0) {
            return new Object[] {};
        }

        Object[] args = new Object[types.length];
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            Class<?> type = types[i];
            String name = parameters[i].getName();

            StringValue stringValue = request.getParameter(name);
            args[i] = stringValue.to(type);
        }
        return args;
    }
}
