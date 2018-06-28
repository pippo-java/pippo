/*
 * Copyright (C) 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.metrics;

import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.Initializer;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.route.RouteTransformer;
import ro.pippo.core.util.ServiceLocator;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manage the {@link MetricRegistry}, {@link MetricsReporter}s, and {@link Metric} route handlers.
 *
 * @author James Moger
 */
@MetaInfServices
public class MetricsInitializer implements Initializer {

    private static final Logger log = LoggerFactory.getLogger(MetricsInitializer.class);

    private MetricRegistry metricRegistry;
    private List<Closeable> reporters;

    @Override
    public void init(Application application) {
        // init metricRegistry
        metricRegistry = (MetricRegistry) application.getLocals().get("metricRegistry");
        if (metricRegistry == null) {
            metricRegistry = new MetricRegistry();
//            application.getLocals().put("metricRegistry", metricRegistry);
        }

        // set created metricRegistry as default
        SharedMetricRegistries.setDefault("pippo", metricRegistry);

        // init reporters
        reporters = new ArrayList<>();

        PippoSettings pippoSettings = application.getPippoSettings();
        String applicationName = pippoSettings.getString(PippoConstants.SETTING_APPLICATION_NAME, "Pippo");

        // register optional metrics
        if (pippoSettings.getBoolean("metrics.jvm.enabled", false)) {
            registerAll("jvm.gc", new GarbageCollectorMetricSet());
            registerAll("jvm.memory", new MemoryUsageGaugeSet());
            registerAll("jvm.threads", new ThreadStatesGaugeSet());
            registerAll("jvm.classes", new ClassLoadingGaugeSet());

            log.debug("Registered JVM-Metrics integration");
        }

        // MBeans for VisualVM, JConsole, or JMX
        if (pippoSettings.getBoolean("metrics.mbeans.enabled", false)) {
            JmxReporter reporter = JmxReporter.forRegistry(metricRegistry).inDomain(applicationName).build();
            reporter.start();
            reporters.add(reporter);

            log.debug("Started Pippo Metrics MBeans reporter");
        }

        // add classpath reporters
        for (MetricsReporter reporter : ServiceLocator.locateAll(MetricsReporter.class)) {
            reporter.start(pippoSettings, metricRegistry);
            reporters.add(reporter);
        }

        // add the metrics dispatch listener
        MetricsDispatchListener metricsDispatchListener = new MetricsDispatchListener(metricRegistry);
        application.getRoutePreDispatchListeners().add(metricsDispatchListener);
        application.getRoutePostDispatchListeners().add(metricsDispatchListener);

        // add MetricsTransformer
        RouteTransformer transformer = new MetricsTransformer(metricRegistry);
        application.addRouteTransformer(transformer);
    }

    @Override
    public void destroy(Application application) {
        for (Closeable reporter : reporters) {
            log.debug("Stopping '{}'", reporter.getClass().getName());
            try {
                reporter.close();
            } catch (IOException e) {
                log.error("Failed to stop Metrics reporter", e);
            }
        }
    }

    private void registerAll(String prefix, MetricSet metrics) throws IllegalArgumentException {
        for (Map.Entry<String, Metric> entry : metrics.getMetrics().entrySet()) {
            if (entry.getValue() instanceof MetricSet) {
                registerAll(MetricRegistry.name(prefix, entry.getKey()), (MetricSet) entry.getValue());
            } else {
                metricRegistry.register(MetricRegistry.name(prefix, entry.getKey()), entry.getValue());
            }
        }
    }

}
