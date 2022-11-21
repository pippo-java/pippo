/*
 * Copyright (C) 2017-present the original author or authors.
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
package ro.pippo.pac4j;

import org.pac4j.core.config.Config;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

import java.util.Objects;

/**
 * This handler finishes the login process for an indirect client, based on the {@link #callbackLogic}.
 *
 * The configuration can be provided via the following parameters:
 * {@code config} (security configuration),
 * {@code defaultUrl} (default url after login if none was requested),
 * {@code multiProfile} (whether multiple profiles should be kept) and
 * {@code renewSession} (whether the session must be renewed after login).
 *
 * @author Decebal Suiu
 */
public class Pac4jCallbackHandler implements RouteHandler {

    private final String defaultClient;
    private CallbackLogic callbackLogic = new DefaultCallbackLogic();
    private Config config;
    private String defaultUrl;
    private Boolean renewSession;

    public Pac4jCallbackHandler(Config config) {
        this(config, null);
    }

    public Pac4jCallbackHandler(Config config, String defaultUrl) {
        this(config, defaultUrl, Boolean.FALSE, "");
    }

    public Pac4jCallbackHandler(Config config, String defaultUrl, Boolean renewSession, String defaultClient) {
        this.config = config;
        this.defaultUrl = defaultUrl;
        this.renewSession = renewSession;
        this.defaultClient = defaultClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RouteContext routeContext) {
        Objects.requireNonNull(callbackLogic);
        Objects.requireNonNull(config);

        PippoWebContext webContext = new PippoWebContext(routeContext, config.getSessionStore());

        callbackLogic.perform(webContext, config.getSessionStore(), config, config.getHttpActionAdapter(), defaultUrl, renewSession, defaultClient);
    }

    public CallbackLogic getCallbackLogic() {
        return callbackLogic;
    }

    public Pac4jCallbackHandler setCallbackLogic(CallbackLogic callbackLogic) {
        this.callbackLogic = callbackLogic;

        return this;
    }

    public String getDefaultUrl() {
        return this.defaultUrl;
    }

    public Pac4jCallbackHandler setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;

        return this;
    }

    public Boolean getRenewSession() {
        return renewSession;
    }

    public Pac4jCallbackHandler setRenewSession(Boolean renewSession) {
        this.renewSession = renewSession;

        return this;
    }

}
