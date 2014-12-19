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

import ro.fortsoft.pippo.core.util.StringUtils;

/**
 * A {@link ControllerHandler} that will try to use Java 8+ compiled parameter
 * names if the {@link DefaultControllerHandler} can not identify a name for the
 * method parameter.
 * <p>
 * This handler is only effective if you are compiling with the javac
 * <code>-parameters</code> flag.
 * </p>
 *
 * @author James Moger
 */
public class ParameterizedControllerHandler extends DefaultControllerHandler {

    public ParameterizedControllerHandler(Class<? extends Controller> controllerClass, String methodName) {
        super(controllerClass, methodName);
    }

    @Override
    protected String getParameterName(Method method, int i) {

        String name = super.getParameterName(method, i);

        if (StringUtils.isNullOrEmpty(name)) {
            // parameter is not named via annotation
            // try looking for the parameter name in the compiled .class file
            Parameter parameter = method.getParameters()[i];
            if (parameter.isNamePresent()) {
                name = parameter.getName();
            }
        }

        return name;
    }

}
