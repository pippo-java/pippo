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
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.exception.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

import java.util.Objects;

/**
 * This handler (filter) protects an url, based on the {@link #securityLogic}.
 *
 * The configuration can be provided via the following parameters:
 * {@code config} (security configuration),
 * {@code clients} (list of clients for authentication),
 * {@code authorizers} (list of authorizers),
 * {@code matchers} (list of matchers) and
 * {@code multiProfile} (whether multiple profiles should be kept).
 *
 * @author Decebal Suiu
 */
public class Pac4jSecurityHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(Pac4jSecurityHandler.class);

    private SecurityLogic<Object, PippoWebContext> securityLogic = new DefaultSecurityLogic<>();
    private Config config;
    private String clients;
    private String authorizers;
    private String matchers;
    private Boolean multiProfile;

    public Pac4jSecurityHandler(Config config, String clients) {
        this(config, clients, null, null);
    }

    public Pac4jSecurityHandler(Config config, String clients, String authorizers) {
        this(config, clients, authorizers, null);
    }

    public Pac4jSecurityHandler(Config config, String clients, String authorizers, String matchers) {
        this(config, clients, authorizers, matchers, null);
    }

    public Pac4jSecurityHandler(Config config, String clients, String authorizers, String matchers, Boolean multiProfile) {
        this.config = config;
        this.clients = clients;
        this.authorizers = authorizers;
        this.matchers = matchers;
        this.multiProfile = multiProfile;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RouteContext routeContext) {
        Objects.requireNonNull(securityLogic);
        Objects.requireNonNull(config);

        PippoWebContext webContext = new PippoWebContext(routeContext, config.getSessionStore());

        try {
            securityLogic.perform(webContext, config, (ctx, parameters) -> {
                throw new SecurityGrantedAccessException();
            }, config.getHttpActionAdapter(), clients, authorizers, matchers, multiProfile);
            // stop the processing if no success granted access exception has been raised
            log.debug("Halt the request processing");
        } catch (SecurityGrantedAccessException e) {
            // ignore this exception, it means the access is granted: continue
            log.debug("Received SecurityGrantedAccessException -> continue");
            routeContext.next();
        }
    }

    public SecurityLogic<Object, PippoWebContext> getSecurityLogic() {
        return securityLogic;
    }

    public Pac4jSecurityHandler setSecurityLogic(SecurityLogic<Object, PippoWebContext> securityLogic) {
        this.securityLogic = securityLogic;

        return this;
    }

    public String getAuthorizers() {
        return authorizers;
    }

    public Pac4jSecurityHandler setAuthorizers(String authorizers) {
        this.authorizers = authorizers;

        return this;
    }

    public String getMatchers() {
        return matchers;
    }

    public Pac4jSecurityHandler setMatchers(String matchers) {
        this.matchers = matchers;

        return this;
    }

    public Boolean getMultiProfile() {
        return multiProfile;
    }

    public Pac4jSecurityHandler setMultiProfile(Boolean multiProfile) {
        this.multiProfile = multiProfile;

        return this;
    }

    /**
     * Exception when the access is granted.
     */
    private class SecurityGrantedAccessException extends TechnicalException {

        public SecurityGrantedAccessException() {
            super("access granted");
        }

    }

}
