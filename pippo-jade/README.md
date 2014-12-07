pippo-jade
=====================

[Jade][jade] is the Node template language. [jade4j][jade4j] is a Java implementation of the Jade template language specification.

Getting started
---------------

Setup
-----

1) Add the pippo-jade dependency to your pom.xml:

    <dependency>
        <groupId>ro.fortsoft.pippo</groupId>
        <artifactId>pippo-jade</artifactId>
        <version>${pippo.version}</version>
    </dependency>

2)  Start writing [Jade][jade] templates in the `templates` folder of your application.
**Note:** The default file extension of a Jade template is `.jade` and it may be ommitted from your templates and your Pippo Java code.

Integration
-----

This engine includes some useful Pippo integration features and conveniences like... 

### i18n

You can access your translation resources using the i18n method.

    pippo.i18n("key.name")

You can supply arguments to your *MessageFormat* translation resources too.

    pippo.i18n("key.name", "arg1", "arg2")

### prettytime (relative time)

pippo-jade supports automatically localized [prettytime][prettytime] out-of-the-box and it is very easy to use.

Assuming you are providing a `java.util.Date` instance to prettyTime...

    pippo.prettyTime(myDate)

Will produce something like...

    3 days ago

### formatTime

You can also automatically format localized dates using standard Java date format patterns.

    pippo.formatTime(now)}
    pippo.formatTime(now, "short")
    pippo.formatTime(now, "medium")
    pippo.formatTime(now, "long")
    pippo.formatTime(now, "full")
    pippo.formatTime(now, "HH:mm")
    pippo.formatTime(now, "dd-MM-yyyy HH:mm")
    pippo.formatTime(now, "dd-MM-yyyy HH:mm")

### webjarsAt & publicAt

pippo-jade supports context-aware url generation for your classpath resources using the `webjarsAt` and `publicAt` methods.

```jade
// Stylesheets
link(href=pippo.webjarsAt('bootstrap/3.3.1/css/bootstrap.min.css'), rel="stylesheet")
link(href=pippo.webjarsAt('font-awesome/4.2.0/css/font-awesome.min.css'), rel="stylesheet")
link(href=pippo.publicAt('css/style.css'), rel="stylesheet")

// Scripts
script(src=pippo.webjarsAt('jquery/1.11.1/jquery.min.js'))
script(src=pippo.webjarsAt('bootstrap/3.3.1/js/bootstrap.min.js'))
script(src=pippo.publicAt('js/main.js'))
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

### Error Templates

pippo-jade will render special templates for routing problems and excepions.  You may override these templates 
within your own application.

- `templates/pippo/404notFound.jade`
- `templates/pippo/500interalError.jade`

[jade]: http://jade-lang.com
[jade4j]: https://github.com/neuland/jade4j
[prettytime]: http://ocpsoft.org/prettytime
