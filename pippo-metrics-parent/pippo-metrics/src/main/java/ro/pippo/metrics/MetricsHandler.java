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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Response;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Decebal Suiu
 */
public class MetricsHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(MetricsHandler.class);

    public static final TimeUnit DEFAULT_RATE_UNIT = TimeUnit.SECONDS;
    public static final TimeUnit DEFAULT_DURATION_UNIT = TimeUnit.MILLISECONDS;

    public static final double DEFAULT_RATE_FACTOR = DEFAULT_RATE_UNIT.toSeconds(1);
    public static final double DEFAULT_DURATION_FACTOR = 1.0 / DEFAULT_DURATION_UNIT.toNanos(1);

    final MetricRegistry metricRegistry;

    // used in write snapshot
    TimeUnit durationUnit = DEFAULT_DURATION_UNIT;
    double durationFactor = DEFAULT_DURATION_FACTOR;

    // used in write metered
    TimeUnit rateUnit = DEFAULT_RATE_UNIT;
    double rateFactor = DEFAULT_RATE_FACTOR;

    public MetricsHandler() {
        this(SharedMetricRegistries.getDefault());
    }

    public MetricsHandler(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public MetricsHandler withRateUnit(TimeUnit rateUnit) {
        this.rateUnit = rateUnit;

        return this;
    }

    public MetricsHandler withDurationUnit(TimeUnit durationUnit) {
        this.durationUnit = durationUnit;

        return this;
    }

    @Override
    public void handle(RouteContext routeContext) {
        Response response = routeContext.getResponse().noCache().text();

        try (BufferedWriter writer = new BufferedWriter(response.getWriter())) {
            SortedMap<String, Gauge> gauges = metricRegistry.getGauges();
            if (gauges.size() > 0) {
                writeGauges(gauges, writer);
                writer.newLine();
            }

            SortedMap<String, Counter> counters = metricRegistry.getCounters();
            if (counters.size() > 0) {
                writeCounters(counters, writer);
                writer.newLine();
            }

            SortedMap<String, Histogram> histograms = metricRegistry.getHistograms();
            if (histograms.size() > 0) {
                writeHistograms(histograms, writer);
                writer.newLine();
            }

            SortedMap<String, Meter> meters = metricRegistry.getMeters();
            if (meters.size() > 0) {
                writeMeters(meters, writer);
                writer.newLine();
            }

            SortedMap<String, Timer> timers = metricRegistry.getTimers();
            if (timers.size() > 0) {
                writeTimers(timers, writer);
            }

            writer.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void writeGauges(SortedMap<String, Gauge> gauges, BufferedWriter writer) throws IOException {
        writeBanner("Gauges", writer);
        for (String key : gauges.keySet()) {
            writer.write(key + " = " + gauges.get(key).getValue());
            writer.newLine();
        }
    }

    protected void writeCounters(SortedMap<String, Counter> counters, BufferedWriter writer) throws IOException {
        writeBanner("Counters", writer);
        for (String key : counters.keySet()) {
            writer.write(key + " = " + counters.get(key).getCount());
            writer.newLine();
        }
    }

    protected void writeHistograms(SortedMap<String, Histogram> histograms, BufferedWriter writer) throws IOException {
        writeBanner("Histograms", writer);
        for (String key : histograms.keySet()) {
            writer.write(key);
            writer.newLine();
            writeHistogram(histograms.get(key), writer);
            writer.newLine();
        }
    }

    protected void writeHistogram(Histogram histogram, BufferedWriter writer) throws IOException {
        writer.write("   count = " + histogram.getCount());
        writer.newLine();
        writeSnapshot(histogram.getSnapshot(), false, writer);
    }

    protected void writeMeters(SortedMap<String, Meter> meters, BufferedWriter writer) throws IOException {
        writeBanner("Meters", writer);
        for (String key : meters.keySet()) {
            writer.write(key);
            writer.newLine();
            writeMeter(meters.get(key), writer);
        }
    }

    protected void writeMeter(Meter meter, BufferedWriter writer) throws IOException {
        writeMetered(meter, writer);
    }

    protected void writeTimers(SortedMap<String, Timer> timers, BufferedWriter writer) throws IOException {
        writeBanner("Timers", writer);
        for (String key : timers.keySet()) {
            writer.write(key);
            writer.newLine();
            writeTimer(timers.get(key), writer);
            writer.newLine();
        }
    }

    protected void writeTimer(Timer timer, BufferedWriter writer) throws IOException {
        writeMetered(timer, writer);
        writeSnapshot(timer.getSnapshot(), true, writer);
    }

    protected void writeMetered(Metered metered, BufferedWriter writer) throws IOException {
        writeWithIndent("count = " + metered.getCount(), writer);
        writeWithIndent("mean rate = " + getRateString(metered.getMeanRate()), writer);
        writeWithIndent("1-minute rate = " + getRateString(metered.getOneMinuteRate()), writer);
        writeWithIndent("5-minute rate = " + getRateString(metered.getFiveMinuteRate()), writer);
        writeWithIndent("15-minute rate = " + getRateString(metered.getFifteenMinuteRate()), writer);
        writeWithIndent("rate unit = " + rateUnit, writer);
    }

    protected void writeSnapshot(Snapshot snapshot, boolean duration, BufferedWriter writer) throws IOException {
        writeWithIndent("min = " + getSnapshotValueString(snapshot.getMin(), duration), writer);
        writeWithIndent("max = " + getSnapshotValueString(snapshot.getMax(), duration), writer);
        writeWithIndent("mean = " + getSnapshotValueString(snapshot.getMean(), duration), writer);
        writeWithIndent("stdDev = " + getSnapshotValueString(snapshot.getStdDev(), duration), writer);
        writeWithIndent("median = " + getSnapshotValueString(snapshot.getMedian(), duration), writer);
        writeWithIndent("75%% <= " + getSnapshotValueString(snapshot.get75thPercentile(), duration), writer);
        writeWithIndent("95%% <= " + getSnapshotValueString(snapshot.get95thPercentile(), duration), writer);
        writeWithIndent("98%% <= " + getSnapshotValueString(snapshot.get98thPercentile(), duration), writer);
        writeWithIndent("99%% <= " + getSnapshotValueString(snapshot.get99thPercentile(), duration), writer);
        writeWithIndent("99.9%% <= " + getSnapshotValueString(snapshot.get999thPercentile(), duration), writer);
        if (duration) {
            writeWithIndent("duration unit = " + durationUnit, writer);
        }
    }

    protected String getRateString(double value) {
        return toString(value * rateFactor);
    }

    protected String getSnapshotValueString(double value, boolean duration) {
        return duration ? getDurationString(value) : String.valueOf(value);
    }

    protected String getDurationString(double value) {
        return toString(value * durationFactor);
    }

    protected String toString(double value) {
        return String.format("%.2f", value);
    }

    protected void writeBanner(String banner, BufferedWriter writer) throws IOException {
        writer.write("### " + banner + " ###");
        writer.newLine();
    }

    protected void writeWithIndent(String text, BufferedWriter writer) throws IOException {
        writer.write("   " + text);
        writer.newLine();
    }

}
