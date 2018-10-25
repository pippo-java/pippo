## Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

### [Unreleased][unreleased]

#### Fixed
- [#458]: Java deserialization vulnerability in SerializationSessionDataTranscoder.decode()

#### Changed
- [#466]: Updated `FastJson` tp latest version 1.2.51

#### Added

#### Removed

### [1.11.0] - 2018-10-05

#### Fixed
- [#436]: The PippoSettings file is now read with UTF-8 as the default encoding
- [#438]: Incorrect service file name for template engines

#### Changed
- [#443]: Make methods statics in `IoUtils`
- [#446]: Upgrade `jackson` to latest version (2.9.6)
- [#447]: Upgrade `guice` to latest version (4.2.0)
- [#459]: Update Pebble template engine to 3.0.5
- [#460]: Update Tomcat to 8.5.34

#### Added
- [#437]: Add useful handlers for admin
- Add model field in `TemplateHandler`
- Add masking password feature in `SettingsHandler`
- [#439]: Add `DirEntry` comparator in `DirectoryHandler`
- [#452]: Add new headers
- [#456]: Add `CorsHandler` for Cross-origin resource sharing

#### Removed

### [1.10.0] - 2018-07-10

#### Fixed
- [#433]: FileItem's input stream is closed properly now

#### Changed
- Use try-with-resources in IoUtils
- Update Dropwizard's metric to 4.0.2
- Update Undertow to 1.4.25.Final
- Update Pebble template engine to 2.6.1
- Rename `CountedRouteHandler` to `CountedHandler`, `MeteredRouteHandler` to `MeteredHandler`, `TimedRouteHandler` to `TimedHandler`
- [#433]: Call SharedMetricRegistries.setDefault in MetricsInitializer

#### Added
- [#427]: Add option converterClass to `@ParamField`
- [#428]: Add support for Prometheus (metrics)
- Add `getResourceAsString` method in `IoUtils`

#### Removed

### [1.9.0] - 2018-05-10

#### Fixed
- [#420]: Trailing slashes removed from registered routes

#### Changed
- [#418]: Convert to float, double and BigDecimal according Locale
- [#419]: Uses Locale in request context
- [#421]: Update to Freemarker 2.3.28

#### Added

#### Removed

### [1.8.0] - 2018-02-24

#### Fixed
- [#410]: Any syntax error in freemarker template results in "PippoRuntimeException: Recursion in error handler" exception

#### Changed
- Make setters fluent in `Pac4jCallbackHandler` (pippo-pac4j module)
- Move logging from debug to trace in `DefaultUriMatcher#addUriPattern` (less noise)
- Feat settings: remove braces from start and end in `getStrings`
- Feat settings: add helper methods to get list of float or double
- [#412]: Return immutable `Set` when `ParameterValue#toSet` is called

#### Added
- Add automatically `PippoNopHttpActionAdapter` in `SettingsConfigFactory#build` (pippo-pac4j module)

#### Removed

### [1.7.0] - 2017-12-12

#### Fixed
- [#367]: Redirecting to named routes using setPippoFilterPath settings (improvement)

#### Changed
- Downgrade TJWS version to 3.0.10.Final because the last version is deprecated

#### Added
- [#408]: Add security (PAC4J) module

#### Removed

### [1.6.0] - 2017-11-18

#### Fixed
- [#382]: Missing content type on Response for TJWS server
- [#385]: JedisFactory not initializing redis URI
- [#387]: Intercepted bean method public final Controller.getRequest() cannot be declared final
- [#394]: getResponse().status(200) return 404 code
- [#396]: ErrorHandler not properly working with Controllers and CheckedExceptions
- [#400]: Duplicate routes being created

#### Changed

#### Added
- [#384]: Add full support to configure Undertow server
- [#388]: TemplateEngine customization and extension support
- [#397]: Add path params in websocket uri
- [#404]: Add Polish translation
- Add OPTIONS to Routing

#### Removed

### [1.5.0] - 2017-08-07

#### Fixed
- [#367]: Redirecting to named routes using `setPippoFilterPath` settings
- Stop web server in `PippoRule`
- [#368]: Hot deployment doesn't work in Jetty
- [#381]: Error in Pippo-TJWS module

#### Changed 
- Replace `ALL` route with `ANY` (deprecate `ALL`)
- Update `Pebble` template engine to 2.4.0
- Update `Jetty` to 9.4.6.v20170531
- Update `Undertow` to 1.4.12.Final
- Update `RestAssured` used by `PippoTest` to 2.9.0
- [#378]: Simplify `JettyServer`
- Update `TJWS` to 3.1.4.Final
- Simplify `TomcatServer` (delete executor service)

#### Added
- [#363]: Route `OPTIONS`
- [#366]: Add `TrailingSlashHandler`
- Add `CONNECT` method
- Add log entry in Less and Sass
- [#377]: Handle keystore paths that are relative to Classpath

#### Removed

### [1.4.0] - 2017-05-03

#### Fixed

#### Changed
- [#361]: Move PippoFilter instantiation from Pippo class to the WebServer implementations (breaking change)

#### Added
- Log exception for `PippoFilter.init` method
- [#360]: Websocket support (Jetty and Undertow)

#### Removed

### [1.3.0] - 2017-04-04

#### Fixed
- [353]: Two annotations named Param
- [356]: `ClasspathResourceHandler` Directory Traversal Bug (Security)

#### Changed

#### Added
- [#352]: NotFound (CatchAll) route handler
- [#354]: Add possibility in Controller to return the template rendered string
- [#355]: Hot reloading

#### Removed

### [1.2.0] - 2017-02-17

#### Fixed
- Synchronize cache in `SingletonControllerFactory`

#### Changed
- Rename `RequestLanguageFilter` to `LanguageHandler`

#### Added
- Add `getMessage` method helper/shortcut in `RouteContext`
- [#341]: New controller concept
- [#344]: Helper method in `Response` for setting filename
- [#346]: Expose method to reset the response
- [#347]: Make the `Route` available in `RouteHandler`
- [#350]: Add possibility to use a singleton `Controller` (one instance for all requests)
- [#253]: Named route group
- [#348]: Support attributes in RouteGroup
- Add `getMessages` method helper in `RouteContext`
- Add `getSettings` method helper in `RouteContext`
- Add `getMessages` and `getSettings` methods helper in `Controller`

#### Removed

### [1.1.0] - 2017-01-04

#### Fixed
- [#337]: Cannot run the MemCached unit tests
- [#338]: Consistency in Parameter Value conversion

#### Changed
- [#329]: Make Pippo class a little bit more expressive
- Improve Lambda code (avoid parentheses around a single parameter)
- [#330]: Make public `Route.setAbsoluteUriPattern` method
- [#332]: Relax the signature of WebServer.addListener method
- Return generic type for `RouteContext.getApplication` (no need for cast)
- Set the application as an attribute of the servlet container (ServletContext)
- Mark `RouteHandler` as `FunctionalInterface`

#### Added
- Use `gzip` compression where it's possible; see [#331]
- Add `getWriter` method to `Response`

#### Removed

### [1.0.0] - 2016-11-21

#### Fixed

#### Changed
- [#327]: Improve PippoTest (add the possibility to set a custom web server or to set `pippoFilterPath`)

#### Added
- Force `pippo.mode` on TEST in `PippoTest`

#### Removed

### [0.10.0] - 2016-11-07

#### Fixed
- [#318]: Non-debug error handler

#### Changed
- [#315]: Improve the route group concept

#### Added
- [#317]: Add `setFileExtension` method to `TemplateEngine`
- [#321]: Add possibility to register filter, servlet, listener
- [#323]: Inject `Application` instance in `ServletContext`
- [#326]: Add `SingletonControllerFactory`

#### Removed

### [0.9.1] - 2016-08-27

#### Fixed
- Fix stupid NPE in Pippo.start
- [#308]: Fix NPE when Working with PippoTest
- [#310]: `MongoDBSessionDataStorageTest` is failing

#### Changed
- Refine the use of `@MetaInfServices` when it's possible (remove annotation's parameter in some cases)

#### Added
- [#309]: Expose the values attribute of `ParameterValue` via `getValues:String[]`

#### Removed
- Remove unused class `PippoTemplateLocator` from `pippo-trimou`

### [0.9.0] - 2016-08-27

#### Fixed
- [#262]: Errors Configuring Underlying Tomcat Server to use HTTPS without Client Auth
- [#265]: JettyServer attempts to use keystore password as truststore file path
- [#293]: JettyServer.start() blocks due to Jetty server.join()
- [#305]: Trimou TEMPLATE_LOCATOR_INVALID_CONFIGURATION running fat jar
- [#306]: Length issues with unicode characters

#### Changed
- Upgrade [pippo-undertow] to Undertow 1.3.24
- Use `org.kohsuke.metainf-services:meta-services` annotation processor to automatically generate all META-INF/services files
- [pippo-csv] now properly collects all fields in a class hierarchy when deserializing objects
- [pippo-test] Automatically initialize RestAssurred with Pippo ContentType engines
- Upgrade [pippo-trimou] to Trimou 1.8.4
- Upgrade [pippo-xstream] to XStream 1.4.
- Upgrade [pippo-snakeyaml] to SnakeYaml 1.17
- Upgrade [pippo-fastjson] to FastJSON 1.2.8
- Upgrade [pippo-groovy] to Groovy 2.4.6
- Upgrade [pippo-jackson] to Jackson 2.7.3
- Upgrade [pippo-jetty] to Jetty 9.3.8
- Upgrade [pippo-tomcat] to Tomcat 8.0.33
- Upgrade [pippo-jade] to Jade 1.1.4
- Upgrade [pippo-pebble] to Pebble 2.2.2
- Make `chunked` transfer-encoding optional, not the default
- Make the text/plain content type engine handle returning reasonable types like String, CharSequence, char[], and byte[]
- Reduce the DirectoryHandler logging noise caused by connection resets, broken pipes, and connection timeouts by not logging the IOException stacktrace
- Restructuring of the hierarchy of modules

#### Added
- [#245]: Route groups
- [#258]: Optional logo display in PippoFilter
- [#288]: Extract addRoute, GET, POST, ... methods in Routing interface
- [#299]: Encrypted/signed cookie based session

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

[unreleased]: https://github.com/decebals/pippo/compare/release-1.11.0...HEAD
[1.11.0]: https://github.com/decebals/pippo/compare/release-1.10.0...release-1.11.0
[1.10.0]: https://github.com/decebals/pippo/compare/release-1.9.0...release-1.10.0
[1.9.0]: https://github.com/decebals/pippo/compare/release-1.8.0...release-1.9.0
[1.8.0]: https://github.com/decebals/pippo/compare/release-1.7.0...release-1.8.0
[1.7.0]: https://github.com/decebals/pippo/compare/release-1.6.0...release-1.7.0
[1.6.0]: https://github.com/decebals/pippo/compare/release-1.5.0...release-1.6.0
[1.5.0]: https://github.com/decebals/pippo/compare/release-1.4.0...release-1.5.0
[1.4.0]: https://github.com/decebals/pippo/compare/release-1.3.0...release-1.4.0
[1.3.0]: https://github.com/decebals/pippo/compare/release-1.2.0...release-1.3.0
[1.2.0]: https://github.com/decebals/pippo/compare/release-1.1.0...release-1.2.0
[1.1.0]: https://github.com/decebals/pippo/compare/release-1.0.0...release-1.1.0
[1.0.0]: https://github.com/decebals/pippo/compare/release-0.10.0...release-1.0.0
[0.10.0]: https://github.com/decebals/pippo/compare/release-0.9.1...release-0.10.0
[0.9.1]: https://github.com/decebals/pippo/compare/release-0.9.0...release-0.9.1
[0.9.0]: https://github.com/decebals/pippo/compare/release-0.8.0...release-0.9.0
[0.8.0]: https://github.com/decebals/pippo/compare/release-0.7.0...release-0.8.0
[0.7.0]: https://github.com/decebals/pippo/compare/release-0.6.0...release-0.7.0
[0.6.0]: https://github.com/decebals/pippo/compare/release-0.5.0...release-0.6.0
[0.5.0]: https://github.com/decebals/pippo/compare/release-0.4.2...release-0.5.0
[0.4.2]: https://github.com/decebals/pippo/compare/release-0.4.1...release-0.4.2
[0.4.1]: https://github.com/decebals/pippo/compare/pippo-parent-0.4.0...release-0.4.1
[0.4.0]: https://github.com/decebals/pippo/compare/pippo-parent-0.3.0...pippo-parent-0.4.0
[0.3.0]: https://github.com/decebals/pippo/compare/pippo-parent-0.2.0...pippo-parent-0.3.0
[0.2.0]: https://github.com/decebals/pippo/compare/pippo-parent-0.1.0...pippo-parent-0.2.0

[#460]: https://github.com/pippo-java/pippo/issues/460
[#459]: https://github.com/pippo-java/pippo/issues/459
[#456]: https://github.com/pippo-java/pippo/pull/456
[#452]: https://github.com/pippo-java/pippo/pull/452
[#447]: https://github.com/pippo-java/pippo/pull/447
[#446]: https://github.com/pippo-java/pippo/pull/446
[#443]: https://github.com/pippo-java/pippo/pull/443
[#439]: https://github.com/pippo-java/pippo/issues/439
[#438]: https://github.com/pippo-java/pippo/issues/438
[#437]: https://github.com/pippo-java/pippo/pull/437
[#436]: https://github.com/pippo-java/pippo/pull/436
[#435]: https://github.com/pippo-java/pippo/pull/435
[#433]: https://github.com/pippo-java/pippo/pull/433
[#428]: https://github.com/pippo-java/pippo/pull/428
[#427]: https://github.com/pippo-java/pippo/pull/427
[#421]: https://github.com/decebals/pippo/pull/421
[#420]: https://github.com/decebals/pippo/pull/420
[#419]: https://github.com/decebals/pippo/pull/419
[#418]: https://github.com/decebals/pippo/pull/418
[#412]: https://github.com/decebals/pippo/pull/412
[#410]: https://github.com/decebals/pippo/issues/410
[#408]: https://github.com/decebals/pippo/pull/408
[#404]: https://github.com/decebals/pippo/pull/404
[#400]: https://github.com/decebals/pippo/issues/400
[#397]: https://github.com/decebals/pippo/pull/397
[#396]: https://github.com/decebals/pippo/issues/396
[#394]: https://github.com/decebals/pippo/issues/394
[#388]: https://github.com/decebals/pippo/pull/388
[#387]: https://github.com/decebals/pippo/issues/387
[#385]: https://github.com/decebals/pippo/issues/385
[#384]: https://github.com/decebals/pippo/pull/384
[#382]: https://github.com/decebals/pippo/issues/382
[#381]: https://github.com/decebals/pippo/issues/381
[#378]: https://github.com/decebals/pippo/pull/378
[#377]:https://github.com/decebals/pippo/pull/377
[#368]: https://github.com/decebals/pippo/issues/368
[#367]: https://github.com/decebals/pippo/issues/367
[#366]: https://github.com/decebals/pippo/issues/366
[#363]: https://github.com/decebals/pippo/issues/363
[#361]: https://github.com/decebals/pippo/pull/361
[#360]: https://github.com/decebals/pippo/pull/360
[#356]: https://github.com/decebals/pippo/issues/356
[#355]: https://github.com/decebals/pippo/issues/355
[#354]: https://github.com/decebals/pippo/issues/354
[#353]: https://github.com/decebals/pippo/issues/353
[#352]: https://github.com/decebals/pippo/issues/352
[#350]: https://github.com/decebals/pippo/issues/350
[#348]: https://github.com/decebals/pippo/issues/348
[#347]: https://github.com/decebals/pippo/issues/347
[#346]: https://github.com/decebals/pippo/issues/346
[#344]: https://github.com/decebals/pippo/issues/344
[#341]: https://github.com/decebals/pippo/issues/341
[#338]: https://github.com/decebals/pippo/issues/338
[#337]: https://github.com/decebals/pippo/issues/337
[#332]: https://github.com/decebals/pippo/issues/332
[#331]: https://github.com/decebals/pippo/issues/331
[#330]: https://github.com/decebals/pippo/issues/330
[#329]: https://github.com/decebals/pippo/issues/329
[#327]: https://github.com/decebals/pippo/issues/327
[#326]: https://github.com/decebals/pippo/issues/326
[#323]: https://github.com/decebals/pippo/issues/323
[#321]: https://github.com/decebals/pippo/issues/321
[#318]: https://github.com/decebals/pippo/issues/318
[#317]: https://github.com/decebals/pippo/issues/317
[#315]: https://github.com/decebals/pippo/issues/315
[#310]: https://github.com/decebals/pippo/issues/310
[#309]: https://github.com/decebals/pippo/issues/309
[#308]: https://github.com/decebals/pippo/issues/308
[#306]: https://github.com/decebals/pippo/issues/306
[#305]: https://github.com/decebals/pippo/issues/305
[#299]: https://github.com/decebals/pippo/issues/299
[#293]: https://github.com/decebals/pippo/issues/293
[#288]: https://github.com/decebals/pippo/issues/288
[#265]: https://github.com/decebals/pippo/issues/265
[#262]: https://github.com/decebals/pippo/issues/262
[#258]: https://github.com/decebals/pippo/issues/258
[#253]: https://github.com/decebals/pippo/issues/253
[#245]: https://github.com/decebals/pippo/issues/245
[#241]: https://github.com/decebals/pippo/issues/241
[#234]: https://github.com/decebals/pippo/issues/234
[#233]: https://github.com/decebals/pippo/issues/233
[#231]: https://github.com/decebals/pippo/issues/231
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
