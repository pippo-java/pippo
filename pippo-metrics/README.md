Pippo Metrics
==============

Pippo provides optional integration with [Metrics](http://metrics.dropwizard.io/) for measuring the responsiveness of your application.

Setup
------------------

### Add the Pippo Metrics dependency

```xml
<dependency>
    <groupId>ro.fortsoft.pippo</groupId>
    <artifactId>pippo-metrics</artifactId>
    <version>${pippo.version}</version>
</dependency>
```

### Collecting Metrics

Now you are ready to start annotating your route handler methods or controller methods.

You have several choices in the collection of metrics:

- *Do nothing*
- *Counted*
A counter increments (and optionally decrements) when a method is executed.
- *Metered*
A meter measures the rate of events over time (e.g., “requests per second”). In addition to the mean rate, meters also track 1-, 5-, and 15-minute moving averages.
- *Timed*
A timer measures both the rate that a particular piece of code is called and the distribution of its duration.

1. Start by sprinkling `@Counted`, `@Metered`, or `@Timed` on some of your controller methods.
2. Start up VisualVM (and install the MBeans plugin) or JConsole.
3. Browse your app and refresh the collected metrics.

```java
GET("/", new RouteHandler() {

    @Metered("HelloWorld")
    @Override
    public void handle(Request request, Response response, RouteHandlerChain chain) {
        response.send("Hello World");
    }

});
```

Or a controller example:

```java
public class ContactsController extends Controller {

    @Timed
    public void index() {
        getResponse().render("crud/contacts");
    }

}
```

### Collecting Additional Metrics

#### JVM Metrics

You may optionally enable JVM-level details reporting by setting *metrics.jvm.enabled=true* in your `application.properties` file.

    metrics.jvm.enabled = true

### Reporting Metrics via MBeans for VisualVM, JConsole, or JMX

If you want to expose your metrics to VisualVM, JConsole, or JMX you must enable the MBeans reporter in your `application.properties` file.

    metrics.mbeans.enabled = true

You can view the collected metrics using VisualVM (with the MBeans plugin installed) or using JConsole.
