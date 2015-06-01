## Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

### [Unreleased][unreleased]

#### Fixed
- [#121]: ContentTypeEngines were not properly initialized during the discovery/registration process 
- Fixed multiple regex parameter tokenization

#### Changed
- [#117]: Rework [pippo-spring], [pippo-guice]
- Updated [pippo-fastjson] to FastJSON 1.2.6
- Updated [pippo-undertow] to Undertow 1.2.6
- Updated [pippo-jetty] to Jetty 9.2.11

#### Added
- [#122]: Added a CSRF handler & StatusCodeException class
- [#121]: Added [pippo-jackson]
- [#124]: Automatically encode the parameters values in DefaultRouter.uriFor()
- [#126]: Maven quickstart archetype to build a small Pippo web application
- [#128]: Added support for `Set`, `List`, and any other concrete Collection type
- [#128]: Added support for array query/form parameters like `yada[0]`, `yada[1]`, & `yada[2]`
- [#129]: Added support for `_method` assignment for HTML form POST processing

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

[unreleased]: https://github.com/decebals/pippo/compare/release-0.4.2...HEAD
[0.4.2]: https://github.com/decebals/pippo/compare/release-0.4.1...release-0.4.2
[0.4.1]: https://github.com/decebals/pippo/compare/pippo-parent-0.4.0...release-0.4.1
[0.4.0]: https://github.com/decebals/pippo/compare/pippo-parent-0.3.0...pippo-parent-0.4.0
[0.3.0]: https://github.com/decebals/pippo/compare/pippo-parent-0.2.0...pippo-parent-0.3.0
[0.2.0]: https://github.com/decebals/pippo/compare/pippo-parent-0.1.0...pippo-parent-0.2.0

[#122]: https://github.com/decebals/pippo/issues/122
[#121]: https://github.com/decebals/pippo/issues/121
[#116]: https://github.com/decebals/pippo/issues/116
[#115]: https://github.com/decebals/pippo/issues/115
[#113]: https://github.com/decebals/pippo/issues/113
[#111]: https://github.com/decebals/pippo/issues/111

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
