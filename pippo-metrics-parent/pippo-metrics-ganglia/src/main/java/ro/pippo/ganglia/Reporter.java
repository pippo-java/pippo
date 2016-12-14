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
package ro.pippo.ganglia;

import info.ganglia.gmetric4j.gmetric.GMetric;
import info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.pippo.core.PippoSettings;
import ro.pippo.metrics.MetricsReporter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ganglia.GangliaReporter;

/**
 * Integration of Pippo Metrics with Ganglia.
 *
 * @author James Moger
 *
 */
@MetaInfServices
public class Reporter implements MetricsReporter {

	private final Logger log = LoggerFactory.getLogger(Reporter.class);

	private GangliaReporter reporter;

	@Override
	public void start(PippoSettings settings, MetricRegistry metricRegistry) {
		if (settings.getBoolean("metrics.ganglia.enabled", false)) {

			final String hostname = settings.getLocalHostname();
			final String address = settings.getRequiredString("metrics.ganglia.address");
			final int port = settings.getInteger("metrics.ganglia.port", 8649);
			final long period = settings.getDurationInSeconds("metrics.ganglia.period", 60);

			try {
				GMetric ganglia = new GMetric(address, port, UDPAddressingMode.MULTICAST, 1);
				reporter = GangliaReporter.forRegistry(metricRegistry).convertRatesTo(TimeUnit.SECONDS)
						.convertDurationsTo(TimeUnit.MILLISECONDS).build(ganglia);
				reporter.start(period, TimeUnit.SECONDS);

				log.info("Started Ganglia Metrics reporter for '{}', updating every {} seconds", hostname, period);

			} catch (IOException e) {
				log.error("Failed to start Ganglia reporter!", e);
			}

		} else {
			log.debug("Ganglia Metrics reporter is disabled");
		}
	}

	@Override
	public void close() throws IOException {
		if (reporter != null) {
			reporter.stop();
			log.debug("Stopped Ganglia Metrics reporter");
		}
	}
}
