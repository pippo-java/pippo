package ro.fortsoft.pippo.metrics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for marking a controller method to be Metered for metrics
 * collection.
 *
 * A meter measures the rate of events over time (e.g., “requests per second”).
 * In addition to the mean rate, meters also track 1-, 5-, and 15-minute moving averages.
 *
 * If no name is specified, the controller classname and method name are used.
 *
 * @author James Moger
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Metered {

    String value() default "";

}
