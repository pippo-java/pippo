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
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

import java.util.Objects;

/**
 * This handler handles the (application + identity provider) logout process, based on the {@link #logoutLogic}.
 *
 * The configuration can be provided via the following parameters:
 * {@code config} (the security configuration),
 * {@code defaultUrl} (default logout url),
 * {@code logoutUrlPattern} (pattern that logout urls must match),
 * {@code localLogout} (whether the application logout must be performed),
 * {@code destroySession} (whether we must destroy the web session during the local logout) and
 * {@code centralLogout} (whether the centralLogout must be performed).
 *
 * @author Decebal Suiu
 */
public class Pac4jLogoutHandler implements RouteHandler {

    private LogoutLogic<Object, PippoWebContext> logoutLogic = new DefaultLogoutLogic<>();
    private Config config;
    private String defaultUrl;
    private String logoutUrlPattern;
    private Boolean localLogout;
    private Boolean destroySession;
    private Boolean centralLogout;

    public Pac4jLogoutHandler(Config config) {
        this(config, null);
    }

    public Pac4jLogoutHandler(Config config, String defaultUrl) {
        this(config, defaultUrl, null);
    }

    public Pac4jLogoutHandler(Config config, String defaultUrl, String logoutUrlPattern) {
        this.config = config;
        this.defaultUrl = defaultUrl;
        this.logoutUrlPattern = logoutUrlPattern;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RouteContext routeContext) {
        Objects.requireNonNull(logoutLogic);
        Objects.requireNonNull(config);

        PippoWebContext webContext = new PippoWebContext(routeContext, config.getSessionStore());

        logoutLogic.perform(webContext, config, config.getHttpActionAdapter(), defaultUrl, logoutUrlPattern, localLogout, destroySession, centralLogout);
    }

    public LogoutLogic<Object, PippoWebContext> getLogoutLogic() {
        return logoutLogic;
    }

    public Pac4jLogoutHandler setLogoutLogic(LogoutLogic<Object, PippoWebContext> logoutLogic) {
        this.logoutLogic = logoutLogic;

        return this;
    }

    public String getDefaultUrl() {
        return this.defaultUrl;
    }

    public Pac4jLogoutHandler setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;

        return this;
    }

    public String getLogoutUrlPattern() {
        return logoutUrlPattern;
    }

    public Pac4jLogoutHandler setLogoutUrlPattern(String logoutUrlPattern) {
        this.logoutUrlPattern = logoutUrlPattern;

        return this;
    }

    public Boolean getLocalLogout() {
        return localLogout;
    }

    public Pac4jLogoutHandler setLocalLogout(Boolean localLogout) {
        this.localLogout = localLogout;

        return this;
    }

    public Boolean getDestroySession() {
        return destroySession;
    }

    public Pac4jLogoutHandler setDestroySession(Boolean destroySession) {
        this.destroySession = destroySession;

        return this;
    }

    public Boolean getCentralLogout() {
        return centralLogout;
    }

    public Pac4jLogoutHandler setCentralLogout(Boolean centralLogout) {
        this.centralLogout = centralLogout;

        return this;
    }

}
