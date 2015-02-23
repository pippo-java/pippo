package ro.pippo.demo.custom;

import ro.pippo.core.Application;
import ro.pippo.core.RouteContext;
import ro.pippo.core.RouteContextFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author James Moger
 */
public class CustomRouteContextFactory implements RouteContextFactory {
    @Override
    public RouteContext createRouteContext(Application application, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return new CustomContext(application, httpServletRequest, httpServletResponse);
    }

    @Override
    public void init(Application application) {

    }

    @Override
    public void destroy(Application application) {

    }
}
