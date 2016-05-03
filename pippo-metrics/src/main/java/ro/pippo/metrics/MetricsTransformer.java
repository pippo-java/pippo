package ro.pippo.metrics;

import com.codahale.metrics.MetricRegistry;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.route.CompiledRoute;
import ro.pippo.core.route.CompiledRouteTransformer;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.util.LangUtils;

import java.lang.reflect.Method;

/**
 * @author Decebal Suiu
 */
@MetaInfServices(CompiledRouteTransformer.class)
public class MetricsTransformer implements CompiledRouteTransformer {

    private static final Logger log = LoggerFactory.getLogger(MetricsTransformer.class);

    private final MetricRegistry metricRegistry;

    public MetricsTransformer(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public CompiledRoute transform(CompiledRoute compiledRoute) {
        RouteHandler handler = compiledRoute.getRouteHandler();
        RouteHandler newHandler = handler;

        try {
            Method method = handler.getClass().getMethod("handle", RouteContext.class);

            String metricName = MetricRegistry.name(method.getDeclaringClass(), method.getName());

            if (method.isAnnotationPresent(Metered.class)) {
                log.debug("Found '{}' annotation on method '{}'", Metered.class.getSimpleName(), LangUtils.toString(method));
                // route handler is Metered
                Metered metered = method.getAnnotation(Metered.class);
                if (!metered.value().isEmpty()) {
                    metricName = metered.value();
                }
                newHandler = new MeteredRouteHandler(metricName, handler, metricRegistry);
            } else if (method.isAnnotationPresent(Timed.class)) {
                log.debug("Found '{}' annotation on method '{}'", Timed.class.getSimpleName(), LangUtils.toString(method));
                // route handler is Timed
                Timed timed = method.getAnnotation(Timed.class);
                if (!timed.value().isEmpty()) {
                    metricName = timed.value();
                }
                newHandler = new TimedRouteHandler(metricName, handler, metricRegistry);
            } else if (method.isAnnotationPresent(Counted.class)) {
                log.debug("Found '{}' annotation on method '{}'", Counted.class.getSimpleName(), LangUtils.toString(method));
                // route handler is Counted
                Counted counted = method.getAnnotation(Counted.class);
                if (!counted.value().isEmpty()) {
                    metricName = counted.value();
                }
                newHandler = new CountedRouteHandler(metricName, counted.active(), handler, metricRegistry);
            }
        } catch (Exception e) {
            log.error("Failed to get method?!", e);
        }

        compiledRoute.setRouteHandler(newHandler);

        return compiledRoute;
    }

}
