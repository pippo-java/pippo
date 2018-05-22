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
package ro.pippo.prometheus;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.dhatim.dropwizard.prometheus.PrometheusReporter;
import org.dhatim.dropwizard.prometheus.Pushgateway;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.PippoSettings;
import ro.pippo.metrics.MetricsReporter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Integration of Pippo Metrics with <a href="https://prometheus.io">Prometheus</a>.
 *
 * @author Decebal Suiu
 */
@MetaInfServices
public class Reporter implements MetricsReporter {

    private final Logger log = LoggerFactory.getLogger(Reporter.class);

    private PrometheusReporter reporter;

    @Override
    public void start(PippoSettings settings, MetricRegistry metricRegistry) {
        if (settings.getBoolean("metrics.prometheus.enabled", false)) {
            String hostname = settings.getLocalHostname();
            String address = settings.getRequiredString("metrics.prometheus.address");
            int port = settings.getInteger("metrics.prometheus.port", 2003);
            long period = settings.getDurationInSeconds("metrics.prometheus.period", 60);

            Pushgateway pushgateway = new Pushgateway(address, port);
            reporter = PrometheusReporter.forRegistry(metricRegistry)
                .prefixedWith(hostname)
                .filter(MetricFilter.ALL)
                .build(pushgateway);
            reporter.start(period, TimeUnit.SECONDS);

            log.info("Started Prometheus Metrics reporter for '{}', updating every {} seconds", hostname, period);
        } else {
            log.debug("Prometheus Metrics reporter is disabled");
        }
    }

    @Override
    public void close() throws IOException {
        if (reporter != null) {
            reporter.stop();
            log.debug("Stopped Prometheus Metrics reporter");
        }
    }

}
