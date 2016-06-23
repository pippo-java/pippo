/*
 * Copyright (C) 2015 the original author or authors.
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

import ro.pippo.core.route.Route;
import ro.pippo.core.util.StringUtils;


/**
 * @author ScienJus
 * @date 16/2/14.
 */
public class ControllerRouteGroup {

    private ControllerApplication application;

    private Class<? extends Controller> controllerClass;

    private String uriPattern;

    public ControllerRouteGroup(String uriPattern, Class<? extends Controller> controllerClass, ControllerApplication application) {
        this.uriPattern = uriPattern;
        this.controllerClass = controllerClass;
        this.application = application;
    }

    public Route GET(String uriPattern, String methodName) {
        return application.GET(getAbsoluteUriPattern(uriPattern), controllerClass, methodName);
    }

    public Route GET(String methodName) {
        return application.GET(uriPattern, controllerClass, methodName);
    }

    public Route POST(String uriPattern, String methodName) {
        return application.POST(getAbsoluteUriPattern(uriPattern), controllerClass, methodName);
    }

    public Route POST(String methodName) {
        return application.POST(uriPattern, controllerClass, methodName);
    }

    public Route DELETE(String uriPattern, String methodName) {
        return application.DELETE(getAbsoluteUriPattern(uriPattern), controllerClass, methodName);
    }

    public Route DELETE(String methodName) {
        return application.DELETE(uriPattern, controllerClass, methodName);
    }

    public Route HEAD(String uriPattern, String methodName) {
        return application.HEAD(getAbsoluteUriPattern(uriPattern), controllerClass, methodName);
    }

    public Route HEAD(String methodName) {
        return application.HEAD(uriPattern, controllerClass, methodName);
    }

    public Route PUT(String uriPattern, String methodName) {
        return application.PUT(getAbsoluteUriPattern(uriPattern), controllerClass, methodName);
    }

    public Route PUT(String methodName) {
        return application.PUT(uriPattern, controllerClass, methodName);
    }

    public Route PATCH(String uriPattern, String methodName) {
        return application.PATCH(getAbsoluteUriPattern(uriPattern), controllerClass, methodName);
    }

    public Route PATCH(String methodName) {
        return application.PATCH(uriPattern, controllerClass, methodName);
    }

    public Route ALL(String uriPattern, String methodName) {
        return application.ALL(getAbsoluteUriPattern(uriPattern), controllerClass, methodName);
    }

    public Route ALL(String methodName) {
        return application.ALL(uriPattern, controllerClass, methodName);
    }


    private String getAbsoluteUriPattern(String uriPattern) {
        String absoluteUriPattern = StringUtils.addStart(StringUtils.addStart(uriPattern, "/"), this.uriPattern);
        return "/".equals(absoluteUriPattern) ? absoluteUriPattern : StringUtils.removeEnd(absoluteUriPattern, "/");
    }

}
