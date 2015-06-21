package ro.pippo.demo.less4j;

import ro.pippo.core.Application;
import ro.pippo.core.CssGenerator;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.less4j.Less4jCompiler;
import ro.pippo.sasscompiler.SassCompiler;

/**
 * Created by cory on 20.06.2015.
 */
public class CssCompilerApplication extends Application {

    private CssGenerator lessCss;
    private CssGenerator sassCss;


    @Override
    protected void onInit() {

        lessCss = new Less4jCompiler() {

            @Override
            public String getResource() {
                return null;
            }
        };

        sassCss = new SassCompiler() {
            @Override
            public String getResource() {
                return null;
            }
        };

        GET("/", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                routeContext.render("index.html");
            }

        });

        GET("/less-css", new RouteHandler(){
            @Override
            public void handle(RouteContext routeContext) {
                routeContext.setHeader("Content-type", "text/css");
                routeContext.send(lessCss.getCss());
            }
        });

        GET("/sass-css", new RouteHandler(){
            @Override
            public void handle(RouteContext routeContext) {
                routeContext.setHeader("Content-type", "text/css");
                routeContext.send(sassCss.getCss());
            }
        });
    }
}
