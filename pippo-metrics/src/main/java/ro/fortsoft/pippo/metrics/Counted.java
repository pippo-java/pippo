package ro.fortsoft.pippo.metrics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for marking a controller method to be Counted for metrics
 * collection.
 *
 * A counter increments on method execution and optionally decrements at execution completion.
 *
 * If no name is specified, the controller classname and method name are used.
 *
 * @author James Moger
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Counted {

    String value() default "";

    /**
     * Determines the behavior of the counter.
     * <p/>
     * if false (default), the counter will continuously increment and will
     * indicate the number of times this method has been executed.
     * <p/>
     * if true, the counter will be incremented before the method is executed
     * and will be decremented when method execution has completed - regardless
     * of thrown exceptions.
     * <p/>
     * This is useful for determining the realtime execution status of a method.
     */
    boolean active() default false;

}
