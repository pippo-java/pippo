## Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

### [Unreleased][unreleased]

#### Fixed

#### Changed
- Upgrade [pippo-undertow] to Undertow 1.3.16
- Use `org.kohsuke.metainf-services:meta-services` annotation processor to automatically generate all META-INF/services files

#### Added

#### Removed

### [0.8.0] - 2016-01-29

#### Fixed
- [#209]: Exception handling for requests with arrays and very large numbers
- Fixed parsing of dates, times, and timestamps when parameter value is an empty string
- Fixed StringIndexOutOfBoundsException in ResourceHandler
- Gracefully handle whitespace parameter values

#### Changed
- Upgrade [pippo-tomcat] to Tomcat version 8.0.28
- Upgrade [pippo-weld] to Weld version 2.3.1.Final
- Upgrade [pippo-less4j] to Less4j version 1.15.2
- Upgrade [pippo-undertow] to Undertow 1.3.15
- Move to Java 8
- Move quickstart maven archetype to Java 8
- Upgrade vaadin-sass-compiler to 0.9.13
- Upgrade [pippo-metrics-librato] to Librato 4.1.2.1
- [#241]: Transparently support Java 8 `-parameters` names for controllers

#### Added
- [#228]: Versioning public resources
- Add static factory methods for GET, POST, ... in Route class
- [#230]: Create distribution zip file with `mvn package`
- [#231]: Add [pippo-test] module
- Add DirectoryHandler for serving external directories
- Add [pippo-csv] content-type engine to easily serialize and deserialize to/from CSV

#### Removed

### [0.7.0] - 2015-11-09

#### Fixed
- [#188]: Fix alias substitution in webjars paths
- [#206]: Problem wtih Request.updateEntityFromParameters method
- [#215]: Fixed CSRF guard ignoring POST requests with content-types that specify a charset
- [#219]: Java8 build fails with javadoc warning
- [#233]: Serve static resources from the *root* url

#### Changed
- Updated [pippo-pebble] to Pebble 1.5.2
- Updated [pippo-metrics-librato] to Librato 4.0.1.12
- Updated [pippo-freemarker] to Freemarker 2.3.23
- Updated [pippo-fastjson] to FastJSON 1.2.7
- Updated [pippo-undertow] to Undertow 1.3.5
- Updated [pippo-trimou] to Trimou 1.8.2
- Updated [pippo-jetty] to Jetty 9.3.5
- Updated [pippo-jackson] to Jackson 2.6.3
- Upgrade [pippo-tomcat] to Tomcat version 8.0.24
- Add support for simple integer->boolean conversion in ParameterValue
- [#221]: Use standard java service loader mechanism via ServiceLocator and remove pippo.properties files
- [#189]: Register Json, Xml, and Yaml engines with [pippo-jackson]
- Move demo applications to pippo-demo repository

#### Added
- [#207]: Add PathRegexBuilder 
- [#211]: Add custom Filters, Extensions in PebbleEngine from Application
- [#217]: Add convenience methods for setting date headers
- [#218]: Add Response method to finalize a response and return the OutputStream for custom streaming
- [#220]: Add support for `:alnum:`, `:alpha:`, `:ascii:`, `:digit:`, `:xdigit:` POSIX character classes for URL path parameters. This allows use of UTF-8 in path parameters.

#### Removed

### [0.6.0] - 2015-06-03

#### Fixed
- [#120]: FileResourceHandler usage is not self evident
- [#163]: Recursion in error handler when provoking a 404 with Pebble
- CSRF token was not bound as a local property making templates with forms & CSRF tokens generated in a POST handler fail.

#### Changed
- Updated [pippo-pebble] to Pebble 1.5.0
- Updated [pippo-jade] to Jade4j 0.4.3
- Updated [pippo-trimou] to Trimou 1.8.0
- Updated [pippo-undertow] to Undertow 1.2.8
- Updated [pippo-jackson] to Jackson 2.5.4
- [#152]: Rename maven profile `main` with `standalone` for clearer usage
- Improved support for array types in ParameterValue
- [#181]: [pippo-gson] Serialize and deserialize dates using ISO8601
- [#182]: [pippo-fastjson] Serialize and deserialize dates using ISO8601
- [#183]: [pippo-jackson] Serialize and deserialize dates using ISO8601
- [#184]: Moved JAXB engine into a separate module [pippo-jaxb]

#### Added
- [#35]: Added demo ajax using intercooler.js [pippo-demo-ajax]
- [#141]: Added Apache Tomcat as embedded web server [pippo-tomcat]
- [#144]: Added Apache Velocity as template engine [pippo-velocity]
- [#147]: Web server tuning (being able to tune from PippoSettings)
- [#150]: Added Weld implementation of CDI [pippo-weld], [pippo-demo-weld]
- [#161]: Added route name as first criterion in `RouteContext.uriFor`
- [#162]: Added support for redirect to route via `RouteContext.redirect`
- [#170]: Added less and sass compilers [pippo-less4j], [pippo-sasscompiler], [pippo-demo-css]
- [#180]: Add support for `artifactId` version alias in WebjarsAt declarations (i.e. ${WebjarsAt('jquery/jquery.min.js')})
- [#185]: Allow specifying `Accept-Type` with a URI suffix expression (i.e. GET("/contact/{id: [0-9]+}(\.(json|xml|yaml))?", () -> {}));

#### Removed

### [0.5.0] - 2015-06-03

#### Fixed
- [#121]: ContentTypeEngines were not properly initialized during the discovery/registration process
- Fixed multiple regex parameter tokenization
- [#132]: Prevent web server instantiation

#### Changed
- [#117]: Rework [pippo-spring], [pippo-guice]
- Updated [pippo-fastjson] to FastJSON 1.2.6
- Updated [pippo-undertow] to Undertow 1.2.6
- Updated [pippo-jetty] to Jetty 9.2.11
- Updated [pippo-guice] to Guice 4.0
- Updated [pippo-metrics-librato] to 4.0.1.8

#### Added
- [#121]: Added [pippo-jackson]
- [#122]: Added a CSRF handler & StatusCodeException class
- [#124]: Automatically encode the parameters values in DefaultRouter.uriFor()
- [#126]: Maven quickstart archetype to build a small Pippo web application
- [#128], [#131]: Added support for `Set`, `List`, and any other concrete Collection type
- [#128]: Added support for array query/form parameters like `yada[0]`, `yada[1]`, & `yada[2]`
- [#129]: Added support for `_method` assignment for HTML form POST processing
- [#130]: Added support for `_content_type` and `_content` assignment for HTML form POST processing
- [#134]: Added `Response.send(String format, Object... args)`

#### Removed
- Removed pippo-ioc module because it is no longer used anywhere

### [0.4.2] - 2015-04-30

#### Fixed
- Fixed unset RouteContext ThreadLocal when processing ignore paths

#### Added
- [#115], [#116]: Allow specifying a non-root Pippo filter or servlet path

#### Changed
- Throw the target exception rather than the InvocationTargetException in DefaultControllerHandler
- Updated [pippo-undertow] to Undertow 1.2.3

### [0.4.1] - 2015-04-23

#### Fixed
- [#113]: Fixed ControllerRouter ClassCastException
- [#111]: Request.getBody now uses UTF-8 encoding rather than the default character set of the JVM

#### Changed
- Make the session available to the template engines
- Updated [pippo-fastjson] to FastJSON 1.2.5
- Updated [pippo-groovy] to Groovy 2.4.3
- Updated [pippo-pebble] to Pebble 1.4.5
- Updated [pippo-snakeyaml] to SnakeYAML 1.15
- Updated [pippo-trimou] to Trimou 1.7.3
- Updated [pippo-undertow] to Undertow 1.2.0

### [0.4.0] - 2015-03-27
*Massive change, needs more documenting :)*

#### Fixed
#### Changed
- Moved Controller implementation to [pippo-controller]

#### Added
- Added [pippo-fastjson]
- Added [pippo-groovy]
- Added [pippo-guice]
- Added [pippo-gson]
- Added [pippo-metrics]
- Added [pippo-metrics-ganglia]
- Added [pippo-metrics-graphite]
- Added [pippo-metrics-influxdb]
- Added [pippo-metrics-librato]
- Added [pippo-pebble]
- Added [pippo-session]
- Added [pippo-session-cookie]
- Added [pippo-spring]
- Added [pippo-snakeyaml]
- Added [pippo-tjws]
- Added [pippo-trimou]
- Added [pippo-undertow]
- Added [pippo-xstream]

### [0.3.0] - 2014-11-14

#### Changed
- Improved `noCache` implementation

#### Added
- Implemented Controller concept

### [0.2.0] - 2014-11-07

#### Changed
- Updated to Bootstrap 3.3.0

#### Added
- Added a Jade template engine module
- Added file upload support
- Implemented RouteHandlerChain concept
- Added Request.getSession()
- Added Response.getLocals()

### 0.1.0 - 2014-10-30
Initial release.

#### Added
- Added [pippo-core]
- Added [pippo-freemarker]
- Added [pippo-jetty]

[unreleased]: https://github.com/decebals/pippo/compare/release-0.8.0...HEAD
[0.8.0]: https://github.com/decebals/pippo/compare/release-0.7.0...release-0.8.0
[0.7.0]: https://github.com/decebals/pippo/compare/release-0.6.0...release-0.7.0
[0.6.0]: https://github.com/decebals/pippo/compare/release-0.5.0...release-0.6.0
[0.5.0]: https://github.com/decebals/pippo/compare/release-0.4.2...release-0.5.0
[0.4.2]: https://github.com/decebals/pippo/compare/release-0.4.1...release-0.4.2
[0.4.1]: https://github.com/decebals/pippo/compare/pippo-parent-0.4.0...release-0.4.1
[0.4.0]: https://github.com/decebals/pippo/compare/pippo-parent-0.3.0...pippo-parent-0.4.0
[0.3.0]: https://github.com/decebals/pippo/compare/pippo-parent-0.2.0...pippo-parent-0.3.0
[0.2.0]: https://github.com/decebals/pippo/compare/pippo-parent-0.1.0...pippo-parent-0.2.0

[#241]: https://github.com/decebals/pippo/pull/241
[#234]: https://github.com/decebals/pippo/issues/234
[#233]: https://github.com/decebals/pippo/issues/233
[#231]: https://github.com/decebals/pippo/pull/231
[#230]: https://github.com/decebals/pippo/issues/230
[#228]: https://github.com/decebals/pippo/issues/228
[#221]: https://github.com/decebals/pippo/issues/221
[#220]: https://github.com/decebals/pippo/issues/220
[#219]: https://github.com/decebals/pippo/issues/219
[#218]: https://github.com/decebals/pippo/issues/218
[#217]: https://github.com/decebals/pippo/issues/217
[#215]: https://github.com/decebals/pippo/issues/215
[#211]: https://github.com/decebals/pippo/issues/211
[#209]: https://github.com/decebals/pippo/issues/209
[#207]: https://github.com/decebals/pippo/issues/207
[#206]: https://github.com/decebals/pippo/issues/206
[#189]: https://github.com/decebals/pippo/issues/189
[#188]: https://github.com/decebals/pippo/issues/188
[#185]: https://github.com/decebals/pippo/issues/185
[#184]: https://github.com/decebals/pippo/issues/184
[#183]: https://github.com/decebals/pippo/issues/183
[#182]: https://github.com/decebals/pippo/issues/182
[#181]: https://github.com/decebals/pippo/issues/181
[#180]: https://github.com/decebals/pippo/issues/180
[#170]: https://github.com/decebals/pippo/issues/170
[#163]: https://github.com/decebals/pippo/issues/163
[#162]: https://github.com/decebals/pippo/issues/162
[#161]: https://github.com/decebals/pippo/issues/161
[#152]: https://github.com/decebals/pippo/issues/152
[#150]: https://github.com/decebals/pippo/issues/150
[#147]: https://github.com/decebals/pippo/issues/147
[#144]: https://github.com/decebals/pippo/issues/144
[#141]: https://github.com/decebals/pippo/issues/141
[#134]: https://github.com/decebals/pippo/issues/134
[#132]: https://github.com/decebals/pippo/issues/132
[#131]: https://github.com/decebals/pippo/issues/131
[#130]: https://github.com/decebals/pippo/issues/130
[#129]: https://github.com/decebals/pippo/issues/129
[#128]: https://github.com/decebals/pippo/issues/128
[#126]: https://github.com/decebals/pippo/issues/126
[#124]: https://github.com/decebals/pippo/issues/124
[#122]: https://github.com/decebals/pippo/issues/122
[#121]: https://github.com/decebals/pippo/issues/121
[#120]: https://github.com/decebals/pippo/issues/120
[#117]: https://github.com/decebals/pippo/issues/117
[#116]: https://github.com/decebals/pippo/issues/116
[#115]: https://github.com/decebals/pippo/issues/115
[#113]: https://github.com/decebals/pippo/issues/113
[#111]: https://github.com/decebals/pippo/issues/111
[#35]: https://github.com/decebals/pippo/issues/35

[pippo-controller]: https://github.com/decebals/pippo/tree/master/pippo-controller
[pippo-core]: https://github.com/decebals/pippo/tree/master/pippo-core
[pippo-fastjson]: https://github.com/decebals/pippo/tree/master/pippo-fastjson
[pippo-freemarker]: https://github.com/decebals/pippo/tree/master/pippo-freemarker
[pippo-groovy]: https://github.com/decebals/pippo/tree/master/pippo-groovy
[pippo-gson]: https://github.com/decebals/pippo/tree/master/pippo-gson
[pippo-guice]: https://github.com/decebals/pippo/tree/master/pippo-guice
[pippo-jackson]: https://github.com/decebals/pippo/tree/master/pippo-jackson
[pippo-jade]: https://github.com/decebals/pippo/tree/master/pippo-jade
[pippo-jetty]: https://github.com/decebals/pippo/tree/master/pippo-jetty
[pippo-metrics]: https://github.com/decebals/pippo/tree/master/pippo-metrics
[pippo-metrics-ganglia]: https://github.com/decebals/pippo/tree/master/pippo-metrics-ganglia
[pippo-metrics-graphite]: https://github.com/decebals/pippo/tree/master/pippo-metrics-graphite
[pippo-metrics-influxdb]: https://github.com/decebals/pippo/tree/master/pippo-metrics-influxdb
[pippo-metrics-librato]: https://github.com/decebals/pippo/tree/master/pippo-metrics-librato
[pippo-pebble]: https://github.com/decebals/pippo/tree/master/pippo-pebble
[pippo-snakeyaml]: https://github.com/decebals/pippo/tree/master/pippo-snakeyaml
[pippo-session]: https://github.com/decebals/pippo/tree/master/pippo-session
[pippo-session-cookie]: https://github.com/decebals/pippo/tree/master/pippo-session-cookie
[pippo-spring]: https://github.com/decebals/pippo/tree/master/pippo-spring
[pippo-tjws]: https://github.com/decebals/pippo/tree/master/pippo-tjws
[pippo-trimou]: https://github.com/decebals/pippo/tree/master/pippo-trimou
[pippo-undertow]: https://github.com/decebals/pippo/tree/master/pippo-undertow
[pippo-xstream]: https://github.com/decebals/pippo/tree/master/pippo-xstream
[pippo-demo-ajax]: https://github.com/decebals/pippo/tree/master/pippo-demo/pippo-demo-ajax
[pippo-tomcat]: https://github.com/decebals/pippo/tree/master/pippo-tomcat
[pippo-weld]: https://github.com/decebals/pippo/tree/master/pippo-weld
[pippo-demo-weld]: https://github.com/decebals/pippo/tree/master/pippo-demo/pippo-demo-weld
[pippo-velocity]: https://github.com/decebals/pippo/tree/master/pippo-velocity
[pippo-less4j]: https://github.com/decebals/pippo/tree/master/pippo-less4j
[pippo-sasscompiler]: https://github.com/decebals/pippo/tree/master/pippo-sasscompiler
[pippo-demo-css]: https://github.com/decebals/pippo/tree/master/pippo-demo/pippo-demo-css
[pippo-jaxb]: https://github.com/decebals/pippo/tree/master/pippo-jaxb
[pippo-test]: https://github.com/decebals/pippo/tree/master/pippo-test
[pippo-csv]: https://github.com/decebals/pippo/tree/master/pippo-csv
