package ro.pippo.demo.custom;

import ro.pippo.core.Application;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.DefaultRouteContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author James Moger
 */
public class CustomContext extends DefaultRouteContext {

    protected CustomContext(Application application, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        super(application, httpServletRequest, httpServletResponse);
    }

    public void sendHelloMyFriend() {
        getResponse().status(HttpConstants.StatusCode.OK).text().send("Hello, my friend!");
    }
}
