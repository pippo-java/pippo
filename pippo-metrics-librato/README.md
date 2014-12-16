## Reporting Pippo Metrics to Librato

[Librato](http://metrics.librato.com) is a cloud-based metrics database and dashboard service.

Add the following dependency to your application `pom.xml`.

```xml
<dependency>
    <groupId>ro.fortsoft.pippo</groupId>
    <artifactId>pippo-metrics-librato</artifactId>
    <version>${pippo.version}</version>
</dependency>
```

Add the following settings to your `application.properties`.

    metrics.librato.enabled = true
    metrics.librato.username = person@example.com
    metrics.librato.apikey = 12345cafebabe
    metrics.librato.period = 60 seconds

