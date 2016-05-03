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

import com.codahale.metrics.JmxReporter;
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
import ro.pippo.core.route.CompiledRouteTransformer;
import ro.pippo.core.util.ServiceLocator;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pippo Metrics is the singleton for managing the MetricRegistry,
 * MetricReporters, and Metric route handlers.
 *
 * @author James Moger
 */
@MetaInfServices(Initializer.class)
public class PippoMetrics implements Initializer {

    private static final Logger log = LoggerFactory.getLogger(PippoMetrics.class);

    private final MetricRegistry metricRegistry;
    private final List<Closeable> reporters;

    public PippoMetrics() {
        this(new MetricRegistry());
    }

    public PippoMetrics(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.reporters = new ArrayList<>();
    }

    @Override
    public void init(Application application) {
        PippoSettings pippoSettings = application.getPippoSettings();
        String applicationName = pippoSettings.getString(PippoConstants.SETTING_APPLICATION_NAME, "Pippo");

        /*
         * Register optional metrics
         */
        if (pippoSettings.getBoolean("metrics.jvm.enabled", false)) {
            registerAll("jvm.gc", new GarbageCollectorMetricSet());
            registerAll("jvm.memory", new MemoryUsageGaugeSet());
            registerAll("jvm.threads", new ThreadStatesGaugeSet());
            registerAll("jvm.classes", new ClassLoadingGaugeSet());

            log.debug("Registered JVM-Metrics integration");
        }

        /*
         * MBeans for VisualVM, JConsole, or JMX
         */
        if (pippoSettings.getBoolean("metrics.mbeans.enabled", false)) {
            JmxReporter reporter = JmxReporter.forRegistry(metricRegistry).inDomain(applicationName).build();
            reporter.start();
            reporters.add(reporter);

            log.debug("Started Pippo Metrics MBeans reporter");
        }

        /*
         * Add classpath reporters
         */
        for (MetricsReporter reporter : ServiceLocator.locateAll(MetricsReporter.class)) {
            reporter.start(pippoSettings, metricRegistry);
            reporters.add(reporter);
        }

        // Add the metrics dispatch listener
        MetricsDispatchListener metricsDispatchListener = new MetricsDispatchListener(metricRegistry);
        application.getRoutePreDispatchListeners().add(metricsDispatchListener);
        application.getRoutePostDispatchListeners().add(metricsDispatchListener);

        // Add MetricsTransformer
        CompiledRouteTransformer transformer = new MetricsTransformer(metricRegistry);
        application.getRouter().addCompiledRouteTransformer(transformer);
    }

    @Override
    public void destroy(Application application) {
        for (Closeable reporter : reporters) {
            log.debug("Stopping {}", reporter.getClass().getName());
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
