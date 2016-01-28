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
package ro.pippo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Param;
import ro.pippo.core.ParameterValue;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.util.LangUtils;
import ro.pippo.core.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Decebal Suiu
 */
public class DefaultControllerHandler implements ControllerHandler {

    private static final Logger log = LoggerFactory.getLogger(ControllerHandler.class);
    private static final String FORM = "@form";
    private static final String BODY = "@body";

    protected final Class<? extends Controller> controllerClass;
    protected final String methodName;
    protected final Method method;
    protected String[] parameterNames;

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
    public void handle(RouteContext routeContext) {
        log.debug("Invoke method '{}'", LangUtils.toString(method));
        try {
            ControllerApplication application = (ControllerApplication) routeContext.getApplication();

            // create the controller instance
            Controller controller = application.getControllerFactory().createController(controllerClass);

            application.getControllerInstantiationListeners().onInstantiation(controller);

            // init controller
            controller.init(routeContext);
            application.getControllerInitializationListeners().onInitialize(controller);

            // invoke action (a method from controller)
            application.getControllerInvokeListeners().onInvoke(controller, method);

            Object[] args = prepareMethodArgs(routeContext);
            method.invoke(controller, args);
        } catch (InvocationTargetException e) {
            Throwable x = e.getTargetException();
            if (x instanceof PippoRuntimeException) {
                throw (PippoRuntimeException) x;
            }
            throw new PippoRuntimeException(x);
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }

        routeContext.next();
    }

    protected Method findMethod(Class<? extends Controller> controllerClass, String methodName) {
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
                        if (isBodyParameter(controllerMethod, i)) {
                            // entity built from request body
                            parameterNames[i] = BODY;
                        } else if (isFormParameter(controllerMethod, i)) {
                            // entity built from parameters
                            parameterNames[i] = FORM;
                        } else {
                            // confirm parameter type is supported
                            Class<?> type = types[i];
                            if (Collection.class.isAssignableFrom(type)) {
                                if (type.isInterface() && !(Set.class == type || List.class == type)) {
                                    throw new PippoRuntimeException(
                                        "Controller method '{}' parameter {} of type '{}' is not a supported Collection type!",
                                        LangUtils.toString(method), i, type.getSimpleName());
                                }

                                Class<?> genericType = getParameterGenericType(method, i);
                                ParameterValue testValue = new ParameterValue();
                                testValue.to(genericType);
                            } else {
                                ParameterValue testValue = new ParameterValue();
                                testValue.to(type);
                            }

                            // confirm parameter is named
                            String parameterName = getParameterName(controllerMethod, i);
                            if (StringUtils.isNullOrEmpty(parameterName)) {
                                throw new PippoRuntimeException(
                                    "Controller method '{}' parameter {} of type '{}' does not specify a name!",
                                    LangUtils.toString(method), i, type.getSimpleName());
                            }

                            parameterNames[i] = parameterName;
                        }
                    }
                } else {
                    throw new PippoRuntimeException(
                        "Found overloaded controller method '{}'. Method names must be unique!",
                        LangUtils.toString(method));
                }
            }
        }

        return controllerMethod;
    }

    @SuppressWarnings("unchecked")
    protected Object[] prepareMethodArgs(RouteContext routeContext) {
        Class<?>[] types = method.getParameterTypes();

        if (types.length == 0) {
            return new Object[]{};
        }

        Object[] args = new Object[types.length];
        for (int i = 0; i < args.length; i++) {
            Class<?> type = types[i];
            String name = parameterNames[i];
            if (BODY.equals(name)) {
                Object value = routeContext.createEntityFromBody(type);
                args[i] = value;
            } else if (FORM.equals(name)) {
                Object value = routeContext.createEntityFromParameters(type);
                args[i] = value;
            } else {
                ParameterValue value = routeContext.getParameter(name);
                if (Collection.class.isAssignableFrom(type)) {
                    Class<?> genericType = getParameterGenericType(method, i);
                    if (Set.class == type) {
                        args[i] = value.toSet(genericType);
                    } else if (List.class == type) {
                        args[i] = value.toList(genericType);
                    } else {
                        args[i] = value.toCollection((Class<? extends Collection>) type, genericType, null);
                    }
                } else {
                    args[i] = value.to(type);
                }
            }
        }

        return args;
    }

    protected String getParameterName(Method method, int i) {
        Annotation annotation = getAnnotation(method, i, Param.class);
        if (annotation != null) {
            return ((Param) annotation).value();
        }

        return null;
    }

    protected Class<?> getParameterGenericType(Method method, int i) {
        Type genericType = method.getGenericParameterTypes()[i];
        if (!ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
            throw new PippoRuntimeException("Please specify a generic parameter type for '{}', parameter {} of '{}'",
                method.getParameterTypes()[i].getName(), i, LangUtils.toString(method));
        }

        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        try {
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            throw new PippoRuntimeException("Please specify a generic parameter type for '{}', parameter {} of '{}'",
                method.getParameterTypes()[i].getName(), i, LangUtils.toString(method));
        }
    }

    protected boolean isBodyParameter(Method method, int i) {
        return getAnnotation(method, i, Body.class) != null;
    }

    protected boolean isFormParameter(Method method, int i) {
        return getAnnotation(method, i, Form.class) != null;
    }

    protected Annotation getAnnotation(Method method, int i, Class<?> annotationClass) {
        Annotation[] annotations = method.getParameterAnnotations()[i];
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == annotationClass) {
                return annotation;
            }
        }

        return null;
    }

}
