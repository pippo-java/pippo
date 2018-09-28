package ro.pippo.core.route;

import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.StringUtils;

/**
 * Define how CORS requests are handled.
 *
 * <p>The Cross-Origin Resource Sharing standard works by adding new HTTP headers
 * that allow servers to describe the set of origins that are permitted to read
 * that information using a web browser.</p>
 *
 * <p>For more details see: https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS</p>
 *
 * <p>Based on: https://github.com/pac4j/pac4j/blob/3806174df54b939ed2785ee493f63b9851fcd03e/pac4j-core/src/main/java/org/pac4j/core/authorization/authorizer/CorsAuthorizer.java</p>
 */
public class CorsHandlerV2 implements RouteHandler<RouteContext> {

    private String allowOrigin;

    private String exposeHeaders;

    private int maxAge = -1;

    private boolean allowCredentials;

    private String allowMethods;

    private String allowHeaders;

    public CorsHandlerV2(String allowOrigin) {
        if (StringUtils.isNullOrEmpty(allowOrigin)) {
            throw new PippoRuntimeException("allowOrigin cannot be blank");
        }
        this.allowOrigin = allowOrigin;
    }

    @Override
    public void handle(RouteContext context) {
        context.getResponse().header(HttpConstants.Header.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin);

        if (exposeHeaders != null) {
            context.getResponse().header(HttpConstants.Header.ACCESS_CONTROL_EXPOSE_HEADERS, exposeHeaders);
        }

        if (maxAge != -1) {
            context.getResponse().header(HttpConstants.Header.ACCESS_CONTROL_MAX_AGE, "" + maxAge);
        }

        // According to the documentation only if true is what needs to be set
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Credentials#Directives
        if (allowCredentials) {
            context.getResponse().header(HttpConstants.Header.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        if (allowMethods != null) {
            context.getResponse().header(HttpConstants.Header.ACCESS_CONTROL_ALLOW_METHODS, allowMethods);
        }

        if (allowHeaders != null) {
            context.getResponse().header(HttpConstants.Header.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
        }

        if (context.getRequestMethod().equals("OPTIONS")) {
            context.getResponse().accepted();
            return;
        }

        context.next();
    }

    public void setExposeHeaders(String exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public void setAllowMethods(String allowMethods) {
        this.allowMethods = allowMethods;
    }

    public void setAllowHeaders(String allowHeaders) {
        this.allowHeaders = allowHeaders;
    }

}