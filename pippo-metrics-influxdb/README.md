## Reporting Pippo Metrics to InfluxDB

Pippo supports reporting to [InfluxDB](http://influxdb.com).

Add the following dependency to your application `pom.xml`.

```xml
<dependency>
    <groupId>ro.fortsoft.pippo</groupId>
    <artifactId>pippo-metrics-influxdb</artifactId>
    <version>${pippo.version}</version>
</dependency>
```

Add the following settings to your `application.properties`.

    metrics.influxdb.enabled = true
    metrics.influxdb.address = localhost
    metrics.influxdb.port = 8086
    metrics.influxdb.database = mydb
    metrics.influxdb.username = root
    metrics.influxdb.password = root
    metrics.influxdb.period = 60 seconds

