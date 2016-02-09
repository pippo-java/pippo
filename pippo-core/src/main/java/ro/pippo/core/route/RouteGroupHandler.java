package ro.pippo.core.route;

import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ScienJus
 * @date 16/2/9.
 */
public abstract class RouteGroupHandler {

    private String namespace;

    private List<Route> routes = new ArrayList<>();

    public abstract void handle();

    public Route GET(String uriPattern, RouteHandler routeHandler) {
        if (routeHandler instanceof ResourceHandler) {
            throw new PippoRuntimeException("Please use 'addResourceRoute()'");
        }

        Route route = Route.GET(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }

    public Route GET(RouteHandler routeHandler) {
        return GET("", routeHandler);
    }

    public Route POST(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.POST(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }

    public Route POST(RouteHandler routeHandler) {
        return POST("", routeHandler);
    }

    public Route DELETE(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.DELETE(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }

    public Route DELETE(RouteHandler routeHandler) {
        return DELETE("", routeHandler);
    }

    public Route HEAD(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.HEAD(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }

    public Route HEAD(RouteHandler routeHandler) {
        return HEAD("", routeHandler);
    }

    public Route PUT(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.PUT(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }

    public Route PUT(RouteHandler routeHandler) {
        return PUT("", routeHandler);
    }

    public Route PATCH(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.PATCH(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }

    public Route PATCH(RouteHandler routeHandler) {
        return PATCH("", routeHandler);
    }

    public Route ALL(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.ALL(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }
    public Route ALL(RouteHandler routeHandler) {
        return PATCH("", routeHandler);
    }

    public void GROUP(String namespace, RouteGroupHandler routeGroupHandler) {
        if (StringUtils.isNullOrEmpty(namespace)) {
            throw new PippoRuntimeException("The group namespace cannot be null or empty");
        }
        routes.addAll(routeGroupHandler.routes(buildPath(namespace)));
    }

    public List<Route> routes(String namespace) {
        this.namespace = namespace;
        handle();
        return routes;
    }

    private String buildPath(String uriPattern) {
        String routePath;
        if (namespace.endsWith("/")) {
            if (uriPattern.startsWith("/")) {
                routePath = namespace + uriPattern.substring(1);
            } else {
                routePath = namespace + uriPattern;
            }
        } else {
            if (uriPattern.startsWith("/")) {
                routePath = namespace + uriPattern;
            } else {
                routePath = namespace + "/" + uriPattern;
            }
        }
        if (routePath.endsWith("/")) {
            return routePath.substring(0, routePath.length() - 1);
        }
        return routePath;
    }
}
