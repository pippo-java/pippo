pippo-groovy
=====================

[Groovy MarkupTemplateEngine][groovy] is a Groovy-based DSL for generated XML-like markup.

Getting started
---------------

Setup
-----

1) Add the pippo-groovy dependency to your pom.xml:

    <dependency>
        <groupId>ro.fortsoft.pippo</groupId>
        <artifactId>pippo-groovy</artifactId>
        <version>${pippo.version}</version>
    </dependency>

2)  Start writing [Groovy MarkupTemplateEngine][groovy] templates in the `templates` folder of your application.
**Note:** The default file extension of a Groovy template is `.groovy` and may be ommitted from your Pippo Java code but <u>not</u> your templates.

Integration
-----

This engine includes some useful Pippo integration features and conveniences like... 

### i18n

You can access your translation resources using the i18n helper.

    i18n("key.name")

You can supply arguments to your *MessageFormat* translation resources too.

    i18n("key.name", "arg1", "arg2")

### prettytime (relative time)

pippo-groovy supports automatically localized [prettytime][prettytime] out-of-the-box and it is very easy to use.

Assuming you are providing a `java.util.Date` instance to prettyTime...

    prettyTime(myDate)

Will produce something like...

    3 days ago

### formatTime

You can also automatically format localized dates using standard Java date format patterns.

    formatTime(now)}
    formatTime(now, "short")
    formatTime(now, "medium")
    formatTime(now, "long")
    formatTime(now, "full")
    formatTime(now, "HH:mm")
    formatTime(now, "dd-MM-yyyy HH:mm")
    formatTime(now, "dd-MM-yyyy HH:mm")

### webjarsAt & publicAt

pippo-groovy supports context-aware url generation for your classpath resources using the `webjarsAt` and `publicAt` methods.

```groovy
// Stylesheets
link(href: webjarsAt('bootstrap/3.3.1/css/bootstrap.min.css'), rel:'stylesheet')
link(href: webjarsAt('font-awesome/4.2.0/css/font-awesome.min.css'), rel:'stylesheet')
link(href: publicAt('css/style.css'), rel:'stylesheet')

// Scripts
script(src: webjarsAt('jquery/1.11.1/jquery.min.js')) {}
script(src: webjarsAt('bootstrap/3.3.1/js/bootstrap.min.js')) {}
script(src: publicAt('js/main.js')) {}
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

### AngularJS

pippo-groovy comes with out-of-the-box support for the following syntax:

    ng("something | or | other")

The string passed to '`ng` will produce `{{something | or | other}}` in your generated template.

### Error Templates

pippo-groovy will render special templates for routing problems and excepions.  You may override these templates 
within your own application.

- `templates/pippo/404notFound.groovy`
- `templates/pippo/500interalError.groovy`

[groovy]: http://groovy-lang.org/docs/latest/html/documentation/markup-template-engine.html
[prettytime]: http://ocpsoft.org/prettytime
