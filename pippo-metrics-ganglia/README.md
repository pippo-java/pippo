## Reporting Pippo Metrics to Ganglia

Pippo supports reporting to [Ganglia](http://ganglia.info).

Add the following dependency to your application `pom.xml`.

```xml
<dependency>
    <groupId>ro.pippo</groupId>
    <artifactId>pippo-metrics-ganglia</artifactId>
    <version>${pippo.version}</version>
</dependency>
```

Add the following settings to your `application.properties`.

    metrics.ganglia.enabled = true
    metrics.ganglia.address = ganglia.example.com
    metrics.ganglia.port = 8649
    metrics.ganglia.period = 60 seconds

