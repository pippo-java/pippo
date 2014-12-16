## Reporting Pippo Metrics to Graphite

Pippo supports reporting to [Graphite](https://github.com/graphite-project).

Add the following dependency to your application `pom.xml`.

```xml
<dependency>
    <groupId>ro.fortsoft.pippo</groupId>
    <artifactId>pippo-metrics-graphite</artifactId>
    <version>${pippo.version}</version>
</dependency>
```

Add the following settings to your `application.properties`.

    metrics.graphite.enabled = true
    metrics.graphite.address = graphite.example.com
    metrics.graphite.port = 2003
    metrics.graphite.pickled = false
    metrics.graphite.period = 60 seconds

