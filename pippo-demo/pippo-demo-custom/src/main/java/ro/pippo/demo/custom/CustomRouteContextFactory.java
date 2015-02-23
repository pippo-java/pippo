package ro.pippo.demo.custom;

import ro.pippo.core.Application;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.route.RouteContextFactory;
import ro.pippo.core.route.RouteMatch;

import java.util.List;

/**
 * @author James Moger
 */
public class CustomRouteContextFactory implements RouteContextFactory<CustomContext> {

    @Override
    public CustomContext createRouteContext(Application application, Request request, Response response, List<RouteMatch> routeMatches) {
        return new CustomContext(application, request, response, routeMatches);
    }

    @Override
    public void init(Application application) {

    }

    @Override
    public void destroy(Application application) {

    }
}
