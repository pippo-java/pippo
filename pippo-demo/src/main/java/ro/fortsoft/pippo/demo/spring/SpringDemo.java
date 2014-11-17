package ro.fortsoft.pippo.demo.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ro.fortsoft.pippo.core.Pippo;
import ro.fortsoft.pippo.spring.SpringControllerInjector;

/**
 * @author Decebal Suiu
 */
public class SpringDemo {

    public static void main(String[] args) {
        Pippo pippo = new Pippo();
        pippo.getServer().getSettings().staticFilesLocation("/public");
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        pippo.getApplication().getControllerInstantiationListeners().add(new SpringControllerInjector(applicationContext));
        pippo.getApplication().GET("/", ContactsController.class, "index");
        pippo.start();
    }

}
