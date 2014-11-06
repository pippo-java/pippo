Micro Java Web Framework
=====================
It's an open source (Apache license) micro web framework in Java, with minimal dependencies and a quick learning curve.     
The goal of this project is to create a micro web framework in Java that should be easy to use and hack.      
The concept it's not new (I was inspired by Sinatra, Express JS, Play Framework) but my intention is to provide a clean, easy to use and modular solution. Pippo can be uses in small and medium applications and also in applications based on micro services architecture.   
I believe in simplicity and I will try to develop this framework with these words in mind.  

Artifacts
-------------------
- Pippo Core `pippo-core` (jar)
- Pippo Jetty `pippo-jetty` (jar)
- Pippo Freemarker `pippo-fremarker` (jar)

Using Maven
-------------------
In your pom.xml you must define the dependencies to Pippo artifacts and you can do this in two modes.

If you want to use pippo-core together with standard modules (Jetty, Freemarker - Jetty as default embedded web server and Freemarker as default template engine) you must add below lines in your pom.xml file:

```xml
<dependency>
    <groupId>ro.fortsoft.pippo</groupId>
    <artifactId>pippo</artifactId>
    <version>${pippo.version}</version>
    <type>pom</type>
</dependency>
```

For a concrete example see pom.xml file from pippo-demo folder.

On the other hand Pippo is write with modularity in mind.   
For example if you want the default template engine (Freemarker) in your project, you can add the dependency to this in your pom.xml:
```xml
<dependency>
    <groupId>ro.fortsoft.pippo</groupId>
    <artifactId>pippo-core</artifactId>
    <version>${pippo.version}</version>
</dependency>
<dependency>
    <groupId>ro.fortsoft.pippo</groupId>
    <artifactId>pippo-freemarker</artifactId>
    <version>${pippo.version}</version>
</dependency>
```

If you feel comfortable with other template engines (Jade, Mustache, Pebble, Trimou, ThymeLeaf, ...) you must add the dependency to that pippo module if it exists or to create a new (pippo) module that add that template engine in pippo framework.

Also if you want to use the default servlet engine (Jetty) in your project, you can add the dependency to this in your pom.xml:
```xml
<dependency>
    <groupId>ro.fortsoft.pippo</groupId>
    <artifactId>pippo-core</artifactId>
    <version>${pippo.version}</version>
</dependency>
<dependency>
    <groupId>ro.fortsoft.pippo</groupId>
    <artifactId>pippo-jetty</artifactId>
    <version>${pippo.version}</version>
</dependency>
```

where ${pippo.version} is the last Pippo version.

You may want to check for the latest released version using [Maven Search](http://search.maven.org/#search%7Cga%7C1%7Cpippo)

How to use
-------------------
I provide a pippo-demo module that contains two demo applications: SimpleDemo and CrudDemo.

For SimpleDemo you have two java files: SimpleDemo.java and SimpleApplication.java


```java
public class SimpleDemo {

    public static void main(String[] args) {
//        new Pippo().start(); // run the default web server with the default web server settings

        Pippo pippo = new Pippo(new SimpleApplication());
        pippo.getServer().getSettings().staticFilesLocation("/public");
        pippo.start();
    }

}

public class SimpleApplication extends Application {

    @Override
    public void init() {
        super.init();

        GET("/", (request, response, chain) -> response.send("Hello World"));

        GET("/file", (request, response, chain) -> response.file(new File("pom.xml"));


        GET("/json", (request, response, chain) -> {
                Contact contact = new Contact()
                        .setName("John")
                        .setPhone("0733434435")
                        .setAddress("Sunflower Street, No. 6");
                // you can use variant 1 or 2
//                response.contentType(HttpConstants.ContentType.APPLICATION_JSON); // 1
//                response.send(new Gson().toJson(contact)); // 1
                response.json(contact); // 2
         });

        GET("/template", (request, response, chain) -> {
                Map<String, Object> model = new HashMap<>();
                model.put("greeting", "Hello my friend");

                response.render("hello.ftl", model);
        });

        GET("/error", (request, response, chain) -> { throw new RuntimeException("Errorrrrrrrr..."); });

    }

}
```     

After run the application, open your internet browser and check the routes declared in Application (`http://localhost:8338/`, 
`http://localhost:8338/file`, `http://localhost:8338/json`, `http://localhost:8338/error`).

Under the hood
-------------------
First, the framework is splits in modules (`pippo-core`, `pippo-jetty`, `pippo-freemarker`, ...) because I want to use only that modules that are usefully for me. For example if I develop a rest like application for a javascript frontend library (angular, ...) I don't want to use a template engine because my application connects to a database and it delivers only json.  
Also, maybe I want to use an external servlet container (Tomcat for example) and I don't want to use the web server supplied by Pippo. You can eliminate in this scenarios the jetty default server from Pippo (pippo-jetty module).   
Another scenarios is the case when you (as a web designer/developer) are not familiar with Freemarker (the default template engine from pippo) but you know Jade template engine because you have some experience with NodeJS applications. You can eliminate completely in this scenarios the Freemarker from Pippo (pippo-jetty module).   
How is it implemented the modularity in Pippo? Simple, using the ServiceLoader standard mechanism from Java.  
Also you can use the same mechanism to modularize your application using __ServiceLocator__ class from pippo-core.  

In Pippo are few concepts that you would need to know them as simple user:
- Application
- Request
- Response
- Route
- RouteHandler

If you want to extend Pippo (create new module, modify some default behaviors) you would need to know about:
- PippoFilter
- RouteMatcher
- WebServer
- TemplateEngine
- ServiceLocator

The easy mode to run your application si to use Pippo wrapper class.  

__Route__ are URL schema, which describe the interfaces for making requests to your web application. Combining an HTTP request method (a.k.a. HTTP verb) and a path pattern, you define URLs in your application.  
Each route has an associated __RouteHandler__, which does the job of performing any action in the application and sending the HTTP response.  
Routes are defined using an HTTP verb and a path pattern. Any request to the server that matches a route definition is routed to the associated route handler.

```java
GET("/", new RouteHandler() {

    @Override
    public void handle(Request request, Response response) {
        response.send("Hello World");
    }

});

//or more concise using Java 8 lambdas

GET("/", (request, response, chain) -> response.send("Hello World"));
```

Routes in Pippo are created using methods named after HTTP verbs. For instance, in the previous example, we created a route to handle GET requests to the root of the website. You have a corresponding method in Application for all commonly used HTTP verbs (GET, POST, DELETE, HEAD, PUT). For a basic website, only GET and POST are likely to be used.

The route that is defined first takes precedence over other matching routes. So the ordering of routes is crucial to the behavior of an application.   

Each defined route has an __urlPattern__.
The route can be static or dynamic:
- static ("/", "/hello", "/contacts/1")
- dynamic (regex: "/*" or parameterized: "/contact/:id")

As you can see, it's easy to create routes with parameters. A parameter is preceded by a `:`. 

You can retrieve the path parameter value for a request in type safe mode using:

```java
GET("/contact/:id", (request, response, chain) -> {
        int id = request.getParameter("id").toInt(0);    
        String action = request.getParameter("action").toString("new");
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("id", id);
        model.put("action", action)
        response.render("crud/contact.ftl", model);
    });

```

The __Response__ is a wrapper over HttpServletResponse from servlet API and it provides functionality for modifying the response. You can send a char sequence with `send` method, or a file with `file` method, or a json with `json` method. Also you can send a template file merged with a model using `render` method.  

The __Request__ is a wrapper over HttpServletRequest from servlet API.  

When a request is made to the server, which matches a route definition, the associated handlers are called. The __RouteMather__ contains a method `List<RouteMatch> findRoutes(String requestMethod, String requestUri)` that returns all routes which matches a route definition (String requestMethod, String requestUri).  
Why does RouterMatcher have the method findRoutes(...):List<RouteMatch> instead of findRoute(...):RouteMatch? My response is that I want to use the RouteHandler also to define the Filter concept. I don't want to define a new interface Filter with the same signature as the RouteHandler interface.
A __RouteHandler__ has only one method `void handle(Request request, Response response)`. The __handle__ method can be an endpoint or not. A regular RouteHandler is an endpoint, that means that the response is committed in the handle method of that RouteHandler instance. A committed response has already had its status code and headers written. In Response class exists a method `isCommitted()` that tell you if the response is committed or not. The methods from Response that commit a response are: `send`, `json`, `file`, `render`. If you try to commit a response that was already committed (after content has been written) than a PippoRuntimeException will be thrown.
You can see a filter as a RouteHandler that does not commit the response. A filter is typically used to perform a particular piece of functionality either before or after the primary functionality (another RouteHandler) of a web application is performed. The filter might determine that the user does not have permissions to access a particular servlet, and it might send the user to an error page rather than to the requested resource.

```java
// audit filter
GET("/*", (request, response, chain) -> {
    System.out.println("Url: '" + request.getUrl());
    System.out.println("Uri: '" + request.getUri());
    System.out.println("Parameters: " + request.getParameters());
});

GET("/hello",(request, response, chain) -> response.send("Hello World"));
```

You can see in the above example that I put an audit filter in front of all requests.

An __Application__ is a class which associates with an instance of Pippofilter to serve pages over the HTTP protocol. Usually I subclass this class and add my routes in `init()` method.

```java
public class MyDemo {

    public static void main(String[] args) {
//        new Pippo().start(); // run the default web server with the default web server settings

        Pippo pippo = new Pippo(new SimpleApplication());
        pippo.getServer().getSettings().staticFilesLocation("/public");
        pippo.start();
    }

}

public class MyApplication extends Application {

    @Override
    public void init() {
        super.init();

        GET("/", (request, response, chain) -> response.send("Hello World"));
        
    }

}
```     

another approach is:  

```java
public class MyDemo {

    public static void main(String[] args) {
        Pippo pippo = new Pippo();

        // specify the static files location
        pippo.getServer().getSettings().staticFilesLocation("/public");

        // add routes
        pippo.getApplication().GET("/", (request, response, chain) -> response.send("Hello World"));

        // start the embedded server
        pippo.start();
    }

}

```     

Static files
-------------------
Websites generally need to serve additional files such as images, JavaScript, or CSS. In Pippo, we refer to these files as “static files”.

The easiest way of configuring the static files location is to pass a path in the web server settings.

```java
Pippo pippo = new Pippo();
pippo.getServer().getSettings().staticFilesLocation("/public");
```

or 

```java
Pippo pippo = new Pippo();
pippo.getServer().getSettings().externalStaticFilesLocation("/var/myapp/public");
```

You use `staticFilesLocation()` method when you want to serve files from class loader and `externalStaticFilesLocation` when you want to serve files from outside of your application.

The CrudDemo is a good application that demonstrates the concept of static files. In pippo-demo/src/main/resources I created a folder __public__ and I put all assets in that folder (imgs, css, js, fonts, ...).

```
ls public/css/
bootstrap.min.css  style.css
```

You can see that CrudDemo uses bootstrap framework. You can use the bootstrap css in your web page with:

```html
<head>
    <meta charset="utf-8">
    <meta content="IE=edge" http-equiv="X-UA-Compatible">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="/css/style.css" rel="stylesheet">
    <link href="/css/bootstrap.min.css" rel="stylesheet">
</head>
```

Embedded web server
-------------------
TODO

Templates
-------------------
TODO

Runtime mode
-------------------
An application can run in two modes: __DEV__(development) and __PROD__(production).

You can change the runtime mode using the "pippo.mode" system property (`-Dpippo.mode=dev` in command line or `System.setProperty("pippo.mode", "dev")`).  
The default mode is __PROD__.  

For __DEV__ mode in pippo-jetty the cache for static resources is disabled and in pippo-freemarker the cache for templates is disabled also.

You can retrieves the current runtime mode using `RuntimeMode.getCurrent()`.

How to build
-------------------
Requirements: 
- [Git](http://git-scm.com/) 
- JDK 1.7 (test with `java -version`)
- [Apache Maven 3](http://maven.apache.org/) (test with `mvn -version`)

Steps:
- create a local clone of this repository (with `git clone https://github.com/decebals/pippo.git`)
- go to project's folder (with `cd pippo`) 
- build the artifacts (with `mvn clean package` or `mvn clean install`)

After above steps a folder _target_ is created for each module and all goodies are in that folder.

Demo
-------------------
The demo application is in pippo-demo module. The demo module contains two demo applications: SimpleDemo and CrudDemo.  
In CrudDemo contains a Create Retrieve Update Delete demo (with twitter bootstrap). 
    
Mailing list
--------------
Much of the conversation between developers and users is managed through [mailing list] (http://groups.google.com/group/pippo).

Versioning
------------
Pippo will be maintained under the Semantic Versioning guidelines as much as possible.

Releases will be numbered with the follow format:

`<major>.<minor>.<patch>`

And constructed with the following guidelines:

* Breaking backward compatibility bumps the major
* New additions without breaking backward compatibility bumps the minor
* Bug fixes and misc changes bump the patch

For more information on SemVer, please visit http://semver.org/.

License
--------------
Copyright 2014 Decebal Suiu
 
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
the License. You may obtain a copy of the License in the LICENSE file, or at:
 
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
