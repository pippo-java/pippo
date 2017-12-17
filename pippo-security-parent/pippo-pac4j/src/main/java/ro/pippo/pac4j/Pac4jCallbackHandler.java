/*
 * Copyright (C) 2017 the original author or authors.
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

    private CallbackLogic<Object, PippoWebContext> callbackLogic = new DefaultCallbackLogic<>();
    private Config config;
    private String defaultUrl;
    private Boolean multiProfile;
    private Boolean renewSession;

    public Pac4jCallbackHandler(Config config) {
        this(config, null);
    }

    public Pac4jCallbackHandler(Config config, String defaultUrl) {
        this(config, defaultUrl, null);
    }

    public Pac4jCallbackHandler(Config config, String defaultUrl, Boolean multiProfile) {
        this(config, defaultUrl, multiProfile, null);
    }

    public Pac4jCallbackHandler(Config config, String defaultUrl, Boolean multiProfile, Boolean renewSession) {
        this.config = config;
        this.defaultUrl = defaultUrl;
        this.multiProfile = multiProfile;
        this.renewSession = renewSession;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RouteContext routeContext) {
        Objects.requireNonNull(callbackLogic);
        Objects.requireNonNull(config);

        PippoWebContext webContext = new PippoWebContext(routeContext, config.getSessionStore());

        callbackLogic.perform(webContext, config, config.getHttpActionAdapter(), defaultUrl, multiProfile, renewSession);
    }

    public CallbackLogic<Object, PippoWebContext> getCallbackLogic() {
        return callbackLogic;
    }

    public Pac4jCallbackHandler setCallbackLogic(CallbackLogic<Object, PippoWebContext> callbackLogic) {
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

    public Boolean getMultiProfile() {
        return multiProfile;
    }

    public Pac4jCallbackHandler setMultiProfile(Boolean multiProfile) {
        this.multiProfile = multiProfile;

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
