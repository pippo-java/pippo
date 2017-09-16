package ro.pippo.weld;

import com.jayway.restassured.RestAssured;
import org.jboss.weld.environment.se.Weld;
import org.junit.ClassRule;
import org.junit.Test;
import ro.pippo.controller.Controller;
import ro.pippo.controller.ControllerApplication;
import ro.pippo.controller.GET;
import ro.pippo.controller.Path;
import ro.pippo.test.PippoRule;
import ro.pippo.test.PippoTest;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class ControllerInterceptorTest extends PippoTest {

    @ClassRule
    public static PippoRule pippoRule = new PippoRule(new PippoApplication());

    @Test
    public void testControllerWithInterceptor() {
        RestAssured.when().get("/")
            .then().statusCode(200);
    }

    @Inherited
    @InterceptorBinding
    @Retention(RUNTIME)
    @Target({TYPE})
    @interface Secured {
    }

    @Secured
    @Interceptor
    @Priority(Interceptor.Priority.APPLICATION)
    public static class SecuredInterceptor {
        @AroundInvoke
        public Object doSecured(InvocationContext context) throws Exception {
            return context.proceed();
        }
    }

    @Path("/")
    @Secured
    @Dependent
    public static class ControllerWithInterceptor extends Controller {
        @GET
        public void index() {
            // test all controller methods
            getMessages().getAll(getRouteContext());
            getSettings().getLocalHostname();
            getRequest().getClientIp();
            getResponse().send("ok");
        }
    }

    public static class PippoApplication extends ControllerApplication {
        @Override
        protected void onInit() {
            setControllerFactory(new WeldControllerFactory(new Weld().initialize()));
            addControllers(ControllerWithInterceptor.class);
        }
    }

}

