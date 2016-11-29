package ${package};

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;

/**
 * A simple Pippo application.
 *
 * @see ${package}.PippoLauncher#main(String[])
 */
public class PippoApplication extends Application {

    private final static Logger log = LoggerFactory.getLogger(PippoApplication.class);

    @Override
    protected void onInit() {
        getRouter().ignorePaths("/favicon.ico");

        // send 'Hello World' as response
        GET("/", routeContext -> routeContext.send("Hello World"));

        // send a template as response
        GET("/template", routeContext -> {
            String message;

            String lang = routeContext.getParameter("lang").toString();
            if (lang == null) {
                message = getMessages().get("pippo.greeting", routeContext);
            } else {
                message = getMessages().get("pippo.greeting", lang);
            }

            routeContext.setLocal("greeting", message);
            routeContext.render("hello");
        });
    }

}
