package ro.fortsoft.pippo.demo.controller;

import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;

/**
 * Sample controller methods.
 *
 * @author James Moger
 *
 */
public class J8Controller {

    public static void helloStatic(Request request, Response response, RouteHandlerChain chain) {
        response.send("Hello World");
    }

    public void hello(Request request, Response response, RouteHandlerChain chain) {
        response.send("Hello World");
    }

}
