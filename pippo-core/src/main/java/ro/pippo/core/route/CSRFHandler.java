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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.ParameterValue;
import ro.pippo.core.StatusCodeException;
import ro.pippo.core.util.CryptoUtils;
import ro.pippo.core.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * Handler that generates and validates a CSRF token.
 * <p>
 * An attacker can coerce a victims browser to make the following types of requests:
 * <p>
 * GET requests
 * POST requests with a "Content-Type" of "application/x-www-form-urlencoded", "multipart/form-data", and "text/plain".
 * <p>
 * An attacker can not:
 * <p>
 * Coerce the browser to use other request methods such as PUT and DELETE.
 * Coerce the browser to post other content types, such as "application/json".
 * Coerce the browser to send new cookies, other than those that the server has already set.
 * Coerce the browser to set arbitrary headers, other than the normal headers the browser adds to requests.
 * <p>
 * Since GET requests are not meant to be mutative, there is no danger to an application that follows this
 * best practice.
 * <p>
 * Rules:
 * <p>
 * Permit POST if the "Content-Type" is not a guarded type (see above).
 * Permit POST if the "Csrf-Token" header is "nocheck".
 * Permit POST if the "_csrf_token" query parameter or form field matches the session csrf token.
 *
 * @author James Moger
 */
public class CSRFHandler implements RouteHandler<RouteContext> {

    public static final String TOKEN = "_csrf_token";

    public static final String BINDING = "csrfToken";

    private static final Logger log = LoggerFactory.getLogger(CSRFHandler.class);

    private final List<String> guardedTypes = Arrays.asList("application/x-www-form-urlencoded", "multipart/form-data", "text/plain");

    private final String secretKey;

    private final String algorithm;
    /**
     * Constructs an CSRF handler with a dynamically generated SecretKey.
     */
    public CSRFHandler() {
        this(CryptoUtils.generateSecretKey());
    }

    public CSRFHandler(String secretKey) {
        this(secretKey, CryptoUtils.HMAC_SHA256);
    }

    public CSRFHandler(String secretKey, String algorithm) {
        this.secretKey = secretKey;
        this.algorithm = algorithm;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    protected String getSessionCsrfToken(RouteContext context) {
        return context.getSession(TOKEN);
    }

    protected void setSessionCsrfToken(RouteContext context, String token) {
        context.setSession(TOKEN, token);
    }

    protected String getTokenId(RouteContext context) {
        return context.getSession().getId().toString();
    }

    @Override
    public void handle(RouteContext context) {
        // obtain the servlet http method because Pippo allows method spoofing
        String rawMethod = context.getRequest().getHttpServletRequest().getMethod();
        if (HttpConstants.Method.POST.equals(rawMethod)) {

            // Verify the content-type is guarded
            String contentType = new ParameterValue(context.getRequest().getLocale(), context.getHeader("Content-Type")).toString("").toLowerCase();
            contentType = StringUtils.getPrefix(contentType, ';').trim();
            if (!guardedTypes.contains(contentType)) {
                log.debug("Ignoring '{}' request for {} '{}'", contentType, context.getRequestMethod(),
                    context.getRequestUri());
                return;
            }

            // Permit "nocheck" Csrf-Token headers
            String requestToken = context.getHeader("Csrf-Token");
            if ("nocheck".equals(requestToken)) {
                log.debug("Ignoring 'nocheck' request for {} '{}'", context.getRequestMethod(), context.getRequestUri());
                return;
            }

            if (StringUtils.isNullOrEmpty(requestToken)) {
                requestToken = context.getParameter(TOKEN).toString();
            }

            if (StringUtils.isNullOrEmpty(requestToken)) {
                throw new StatusCodeException(HttpServletResponse.SC_FORBIDDEN, "Illegal request, no '{}'!", TOKEN);
            }

            // Validate the request token against the session token
            String sessionToken = getSessionCsrfToken(context);
            if (!requestToken.equals(sessionToken)) {
                throw new StatusCodeException(HttpServletResponse.SC_FORBIDDEN, "Illegal request, invalid '{}'!", TOKEN);
            }

            log.debug("Validated '{}' for {} '{}'", TOKEN, context.getRequestMethod(), context.getRequestUri());

            context.setLocal(BINDING, sessionToken);

        } else if (HttpConstants.Method.GET.equals(rawMethod)) {

            // Generate a CSRF session token on reads
            if (getSessionCsrfToken(context) == null) {
                String sessionId = getTokenId(context);
                String token = CryptoUtils.hmacDigest(sessionId, secretKey, algorithm);
                setSessionCsrfToken(context, token);
                log.debug("Generated '{}' for {} '{}'", TOKEN, context.getRequestMethod(), context.getRequestUri());
            }

            String token = getSessionCsrfToken(context);
            context.setLocal(BINDING, token);
        }

        context.next();
    }
}

