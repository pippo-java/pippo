/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author Decebal Suiu
 */
class ControllerHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(ControllerHandler.class);

    private Class<? extends Controller> controllerClass;
    private String methodName;

    public ControllerHandler(Class<? extends Controller> controllerClass, String methodName) {
        this.controllerClass = controllerClass;
        this.methodName = methodName;
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
            // invoke action (a method from controller)
            Method method = controllerClass.getMethod(methodName, new Class[]{});
            Controller instance = controllerClass.newInstance();
            instance.init(request, response, chain);
            method.invoke(instance, new Object[] {});
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

}
