## Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

### [Unreleased][unreleased]

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

#### Added
- [#35]: Added demo ajax using intercooler.js [pippo-demo-ajax]
- [#141]: Added Apache Tomcat as embedded web server [pippo-tomcat]
- [#144]: Added Apache Velocity as template engine [pippo-velocity]
- [#147]: Web server tuning (being able to tune from PippoSettings)
- [#150]: Added Weld implementation of CDI [pippo-weld], [pippo-demo-weld]
- [#161]: Added route name as first criterion in `RouteContext.uriFor`
- [#162]: Added support for redirect to route via `RouteContext.redirect`
- [#170]: Added less and sass compilers [pippo-less4j], [pippo-sasscompiler], [pippo-demo-css]

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
- Updated Metrics-Librato to 4.0.1.8

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

[unreleased]: https://github.com/decebals/pippo/compare/release-0.5.0...HEAD
[0.5.0]: https://github.com/decebals/pippo/compare/release-0.4.2...release-0.5.0
[0.4.2]: https://github.com/decebals/pippo/compare/release-0.4.1...release-0.4.2
[0.4.1]: https://github.com/decebals/pippo/compare/pippo-parent-0.4.0...release-0.4.1
[0.4.0]: https://github.com/decebals/pippo/compare/pippo-parent-0.3.0...pippo-parent-0.4.0
[0.3.0]: https://github.com/decebals/pippo/compare/pippo-parent-0.2.0...pippo-parent-0.3.0
[0.2.0]: https://github.com/decebals/pippo/compare/pippo-parent-0.1.0...pippo-parent-0.2.0

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
