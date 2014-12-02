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

You can access your translation resources using the i18n helper.

    ${i18n("key.name")}

You can supply arguments to your *MessageFormat* translation resources too.

    ${i18n("key.name", "arg1", "arg2")}

### prettytime (relative time)

pippo-freemarker supports automatically localized [prettytime][prettytime] out-of-the-box and it is very easy to use.

Assuming you are providing a `java.util.Date` instance to Trimou...

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

[freemarker]: http://freemarker.org
[prettytime]: http://ocpsoft.org/prettytime
