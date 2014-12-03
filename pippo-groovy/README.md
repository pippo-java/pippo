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

### AngularJS

pippo-groovy comes with out-of-the-box support for the following syntax:

    ng("something | or | other")

The string passed to '`ng` will produce `{{something | or | other}}` in your generated template.

[groovy]: http://groovy-lang.org/docs/latest/html/documentation/markup-template-engine.html
[prettytime]: http://ocpsoft.org/prettytime
