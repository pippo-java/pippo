package ro.pippo.demo.csscompilers;

import ro.pippo.core.Application;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.less4j.LessResourceHandler;
import ro.pippo.sasscompiler.SassResourceHandler;

/**
 * Created by Daniel Jipa on 20.06.2015.
 */
public class CssCompilerApplication extends Application {

    @Override
    protected void onInit() {
        addPublicResourceRoute();


        addStaticResourceRoute(new LessResourceHandler("/lesscss", "public/less"));
        addStaticResourceRoute(new SassResourceHandler("/sasscss", "public/sass"));

        GET("/", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                routeContext.render("index.html");
            }

        });
    }
}
