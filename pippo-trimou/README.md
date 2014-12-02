pippo-trimou
=====================

[Trimou][trimou] is a [Mustache][mustache] template engine for Java.

Getting started
---------------

Setup
-----

1) Add the pippo-trimou dependency to your pom.xml:

    <dependency>
        <groupId>ro.fortsoft.pippo</groupId>
        <artifactId>pippo-trimou</artifactId>
        <version>${pippo.version}</version>
    </dependency>

2)  Start writing [Mustache][mustache] templates in the `templates` folder of your application.  You may want to review the [Trimou docs](http://trimou.org/doc/latest.html) for any non-standard syntax notes. 
**Note:** The default file extension of a Trimou template is `.mustache` and it may be ommitted from your templates and your Pippo Java code.

Integration
-----

This engine includes some useful Pippo integration features and conveniences like... 

### i18n

You can access your translation resources using the i18n helper.

    {{i18n "key.name"}}

You can supply arguments to your *MessageFormat* translation resources too.

    {{i18n "key.name" "arg1" "arg2"}}

### prettytime (relative time)

pippo-trimou supports automatically localized [prettytime][prettytime] out-of-the-box and it is very easy to use.

Assuming you are providing a `java.util.Date` instance to prettyTime...

    {{myDate.prettyTime}}
    
Will produce something like...

    3 days ago

### formatTime

You can also automatically format localized dates using standard Java date format patterns.

    {{formatTime now}}
    {{formatTime now style="short"}}
    {{formatTime now style="medium"}}
    {{formatTime now style="long"}}
    {{formatTime now style="full"}}
    {{formatTime now pattern="HH:mm"}}
    {{formatTime now pattern="dd-MM-yyyy HH:mm"}}

### AngularJS

Mustache and AngularJS both use the double-brace notation.  There are several options on how to use both technologies together as outlined [here](https://github.com/trimou/trimou/wiki/How-to-render-a-template-with-braces-delimiters).

pippo-trimou comes with out-of-the-box support for the following syntax:

    {{ng something | or | other}}

Anything after the `ng` will be re-injected into the template during the compilation phase.

[trimou]: http://trimou.org
[mustache]: https://mustache.github.io/mustache.5.html
[prettytime]: http://ocpsoft.org/prettytime
