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
package ro.pippo.influxdb;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import metrics_influxdb.InfluxdbHttp;
import metrics_influxdb.InfluxdbReporter;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.pippo.core.PippoSettings;
import ro.pippo.metrics.MetricsReporter;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

/**
 * Integration of Pippo Metrics with <a href="https://www.influxdata.com">InfluxDB</a>.
 *
 * @author James Moger
 */
@MetaInfServices
public class Reporter implements MetricsReporter {

    private final Logger log = LoggerFactory.getLogger(Reporter.class);

    private InfluxdbReporter reporter;

    @Override
    public void start(PippoSettings settings, MetricRegistry metricRegistry) {
        if (settings.getBoolean("metrics.influxdb.enabled", false)) {
            String hostname = settings.getLocalHostname();
            String address = settings.getRequiredString("metrics.influxdb.address");
            int port = settings.getInteger("metrics.influxdb.port", 8086);
            String database = settings.getRequiredString("metrics.influxdb.database");
            String username = settings.getRequiredString("metrics.influxdb.username");
            String password = settings.getRequiredString("metrics.influxdb.password");
            long period = settings.getDurationInSeconds("metrics.influxdb.period", 60);

            try {
                InfluxdbHttp influxdb = new InfluxdbHttp(address, port, database, username, password);
                reporter = InfluxdbReporter.forRegistry(metricRegistry)
                    .prefixedWith(hostname)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .filter(MetricFilter.ALL)
                    .build(influxdb);

                reporter.start(period, TimeUnit.SECONDS);

                log.debug("Started InfluxDB Metrics reporter for '{}', updating every {} seconds", hostname, period);
            } catch (Exception e) {
                log.error("Failed to start InfluxDB reporter!", e);
            }
        } else {
            log.debug("InfluxDB Metrics reporter is disabled");
        }
    }

    @Override
    public void close() throws IOException {
        if (reporter != null) {
            reporter.stop();
            log.debug("Stopped InfluxDB Metrics reporter");
        }
    }

}
