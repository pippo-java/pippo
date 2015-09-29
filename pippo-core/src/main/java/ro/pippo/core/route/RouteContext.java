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
package ro.pippo.core.route;

import ro.pippo.core.Application;
import ro.pippo.core.ParameterValue;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.Session;

import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * @author James Moger
 */
public interface RouteContext {

    public Application getApplication();

    public Request getRequest();

    public Response getResponse();

    public boolean hasSession();

    public Session getSession();

    public void recreateSession();

    public void resetSession();

    public void touchSession();

    public void invalidateSession();

    public <T> T setSession(String name, T t);

    public <T> T getSession(String name);

    public <T> T removeSession(String name);

    public <T> T setLocal(String name, T t);

    public void setLocals(Map<String, Object> locals);

    public <T> T getLocal(String name);

    public <T> T removeLocal(String name);

    public ParameterValue getParameter(String name);

    public String getHeader(String name);

    public <T> T setHeader(String name, T t);

    public Date setHeader(String name, Date date);

    public void flashError(String message, Object... args);

    public void flashWarning(String message, Object... args);

    public void flashInfo(String message, Object... args);

    public void flashSuccess(String message, Object... args);

    public <T> T createEntityFromParameters(Class<T> classOfT);

    public <T> T createEntityFromBody(Class<T> classOfT);

    public String getRequestUri();

    public String getRequestMethod();

    public boolean isRequestMethod(String method);

    public void render(String templateName);

    public void render(String templateName, Map<String, Object> model);

    public void send(CharSequence content);

    public void send(File file);

    public void send(Object object);

    public void redirect(String path);

    public void redirect(String nameOrUriPattern, Map<String, Object> parameters);

    public void next();

    public void runFinallyRoutes();

    public RouteContext text();

    public RouteContext xml();

    public RouteContext json();

    public RouteContext yaml();

    public RouteContext html();

    public RouteContext negotiateContentType();

    public RouteContext status(int code);

    public String uriFor(String nameOrUriPattern, Map<String, Object> parameters);

}
