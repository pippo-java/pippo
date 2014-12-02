pippo-pebble
=====================

[Pebble][pebble] is a modern template engine for Java.

Getting started
---------------

Setup
-----

1) Add the pippo-pebble dependency to your pom.xml:

    <dependency>
        <groupId>ro.fortsoft.pippo</groupId>
        <artifactId>pippo-pebble</artifactId>
        <version>${pippo.version}</version>
    </dependency>

2)  Start writing [Pebble][pebble] templates in the `templates` folder of your application.
**Note:** The default file extension of a Pebble template is `.peb` and it may be ommitted from your templates and your Pippo Java code.

Integration
-----

This engine includes some useful Pippo integration features and conveniences like... 

### i18n

You can access your translation resources using the i18n helper.

    {{ i18n('key.name') }}

You can supply arguments to your *MessageFormat* translation resources too.

    {{ i18n('key.name', 'arg1', 'arg2') }}

### prettytime (relative time)

pippo-pebble supports automatically localized [prettytime][prettytime] out-of-the-box and it is very easy to use.

Assuming you are providing a `java.util.Date` instance to prettyTime...

    {{ myDate | prettyTime }}

Will produce something like...

    3 days ago

### formatTime

You can also automatically format localized dates using standard Java date format patterns.

    {{ myDate | formatTime('short') }}
    {{ myDate | formatTime('medium') }}
    {{ myDate | formatTime('long') }}
    {{ myDate | formatTime('full') }}
    {{ myDate | formatTime('HH:mm') }}
    {{ myDate | formatTime('dd-MM-yyyy HH:mm') }}

### AngularJS

Pebble and AngularJS both use the double-brace notation.

pippo-pebble comes with out-of-the-box support for the following syntax:

    {{ng something | or | other}}

Anything after the `ng` will be re-injected into the template during the compilation phase.

[pebble]: http://www.mitchellbosecke.com/pebble/home
[prettytime]: http://ocpsoft.org/prettytime
