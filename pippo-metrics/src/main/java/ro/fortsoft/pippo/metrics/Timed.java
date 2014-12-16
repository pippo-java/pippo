package ro.fortsoft.pippo.metrics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for marking a controller method to be Timed for metrics
 * collection.
 *
 * A timer measures both the rate that a particular piece of code is called
 * and the distribution of its duration.
 *
 * If no name is specified, the controller classname and method name are used.
 *
 * @author James Moger
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timed {

    String value() default "";

}