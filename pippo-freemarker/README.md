pippo-freemarker
=====================

[Freemarker][freemarker] is a template engine for Java.

Getting started
---------------

Setup
-----

1) Add the pippo-freemarker dependency to your pom.xml:

    <dependency>
        <groupId>ro.fortsoft.pippo</groupId>
        <artifactId>pippo-freemarker</artifactId>
        <version>${pippo.version}</version>
    </dependency>

2)  Start writing [Freemarker][freemarker] templates in the `templates` folder of your application.
**Note:** The default file extension of a Freemarker template is `.ftl` and it may be ommitted from your templates and your Pippo Java code.

Integration
-----

This engine includes some useful Pippo integration features and conveniences like... 

### i18n

You can access your translation resources using the i18n method.

    ${i18n("key.name")}

You can supply arguments to your *MessageFormat* translation resources too.

    ${i18n("key.name", "arg1", "arg2")}

### prettytime (relative time)

pippo-freemarker supports automatically localized [prettytime][prettytime] out-of-the-box and it is very easy to use.

Assuming you are providing a `java.util.Date` instance to prettyTime...

    ${prettyTime(myDate)}

Will produce something like...

    3 days ago

### formatTime

You can also automatically format localized dates using standard Java date format patterns.

    ${formatTime(now)}
    ${formatTime(now, "short")}
    ${formatTime(now, "medium")}
    ${formatTime(now, "long")}
    ${formatTime(now, "full")}
    ${formatTime(now, "HH:mm")}
    ${formatTime(now, "dd-MM-yyyy HH:mm")}
    ${formatTime(now, "dd-MM-yyyy HH:mm")}

### webjarsAt & publicAt

pippo-freemarker supports context-aware url generation for your classpath resources using the `webjarsAt` and `publicAt` methods.

```html
<!-- Stylesheets -->
<link href="${webjarsAt('bootstrap/3.3.1/css/bootstrap.min.css')}" rel="stylesheet">
<link href="${webjarsAt('font-awesome/4.2.0/css/font-awesome.min.css')}" rel="stylesheet">
<link href="${publicAt('css/style.css')}" rel="stylesheet">

<!-- Scripts -->
<script src="${webjarsAt('jquery/1.11.1/jquery.min.js')}"></script>
<script src="${webjarsAt('bootstrap/3.3.1/js/bootstrap.min.js')}"></script>
<script src="${publicAt('js/main.js')}"></script>
```

**NOTE:** Use of these methods require that you have registered a `WebjarsResourceHandler` and/or a `PublicResourcehandler`.

```java
public class MyApplication extends Application {
 @Override
    public void init() {
        super.init();

        // add classpath resource handlers
        GET(new WebjarsResourceHandler());
        GET(new PublicResourceHandler());
        ...
    }
}
```

[freemarker]: http://freemarker.org
[prettytime]: http://ocpsoft.org/prettytime
