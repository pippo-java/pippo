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

You can access your translation resources using the i18n helper.

    pippo.i18n("key.name")

You can supply arguments to your *MessageFormat* translation resources too.

    pippo.i18n("key.name", "arg1", "arg2")

### prettytime (relative time)

pippo-groovy supports automatically localized [prettytime][prettytime] out-of-the-box and it is very easy to use.

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

[jade]: http://jade-lang.com
[jade4j]: https://github.com/neuland/jade4j
[prettytime]: http://ocpsoft.org/prettytime
