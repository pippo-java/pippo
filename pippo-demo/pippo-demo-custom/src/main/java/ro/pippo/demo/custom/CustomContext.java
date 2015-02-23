package ro.pippo.demo.custom;

import ro.pippo.core.Application;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.route.DefaultRouteContext;
import ro.pippo.core.route.RouteMatch;

import java.util.List;

/**
 * @author James Moger
 */
public class CustomContext extends DefaultRouteContext {

    protected CustomContext(Application application, Request request, Response response, List<RouteMatch> routeMatches) {
        super(application, request, response, routeMatches);
    }

    public void sendHelloMyFriend() {
        getResponse().ok().text().send("Hello, my friend!");
    }
}
