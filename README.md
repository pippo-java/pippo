Micro Java Web Framework
=====================
     ____  ____  ____  ____  _____
    (  _ \(_  _)(  _ \(  _ \(  _  )
     ) __/ _)(_  ) __/ ) __/ )(_)( 
    (__)  (____)(__)  (__)  (_____)

It's an open source (Apache license) micro web framework in Java, with minimal dependencies and a quick learning curve.     
The goal of this project is to create a micro web framework in Java that should be easy to use and hack.      
The concept it's not new (I was inspired by Sinatra, Express JS, Play Framework) but my intention is to provide a clean, easy to use and modular solution. Pippo can be used in small and medium applications and also in applications based on micro services architecture.   
I believe in simplicity and I will try to develop this framework with these words in mind.  

The core is very small (43k) and I intend to keep this module as small/simple as possible and to push new functionalities in pippo modules and third-party repositories/modules.

The framework is based on Java Servlet 3.0 and requires Java 7. 

Artifacts
-------------------
- Core
     - Pippo Core `pippo-core` (jar)
- Web Server
     - [Pippo Jetty](https://github.com/decebals/pippo/tree/master/pippo-jade) `pippo-jetty` (jar)
- JSON
     - Pippo Gson `pippo-gson` (jar)
     - Pippo Fastjson `pippo-fastjson` (jar)
- XML
     - Pippo XStream `pippo-xstream` (jar)
- Template Engine
     - [Pippo Freemarker](https://github.com/decebals/pippo/tree/master/pippo-freemarker) `pippo-freemarker` (jar)
     - [Pippo Groovy](https://github.com/decebals/pippo/tree/master/pippo-groovy) `pippo-groovy` (jar)
     - [Pippo Jade](https://github.com/decebals/pippo/tree/master/pippo-jade) `pippo-jade` (jar)
     - [Pippo Pebble](https://github.com/decebals/pippo/tree/master/pippo-pebble) `pippo-pebble` (jar)
     - [Pippo Trimou](https://github.com/decebals/pippo/tree/master/pippo-trimou) `pippo-trimou` (jar)
- IoC/DI
     - Pippo IoC `pippo-ioc` (jar)
     - Pippo Spring `pippo-spring` (jar)
     - Pippo Guice `pippo-guice` (jar)

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

> **NOTE**
> Pippo is built using Java 1.7 (and NOT Java 1.8) but we will use lambdas in examples to show shorter code. 

```java
public class SimpleDemo {

    public static void main(String[] args) {
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
            //response.contentType(HttpConstants.ContentType.APPLICATION_JSON); // 1
            //response.send(new Gson().toJson(contact)); // 1
            response.json(contact); // 2
         });

        GET("/template", (request, response, chain) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("greeting", "Hello my friend");
            response.render("hello", model);
        });

        GET("/error", (request, response, chain) -> { throw new RuntimeException("Error"); });
    }

}
``` 

where `Contact` is a simple POJO:
```java
public class Contact  {

    private int id;
    private String name;
    private String phone;
    private String address;
    
    // getters ans setters

}
```

After run the application, open your internet browser and check the routes declared in Application (`http://localhost:8338/`, 
`http://localhost:8338/file`, `http://localhost:8338/json`, `http://localhost:8338/error`).

Controllers
-------------------
Another approach to handling a request and producing a response is using Controllers. After routing has determined what controller to use, an action method will be invoked.
In Pippo, controllers are instances of `Controller`.

Defining a new controller is simple:
```java
public class ContactsController extends Controller {

    public void index() {
        getResponse().render("crud/contacts");
    }

}
```

Methods attached to the controller (for example `index` from above snippet) are known as action methods. When Pippo receives a request, it will create a new instance of the controller and call the appropriate action method.
You can register a controller's action in application with a simple line:
```java
public class ControllerDemo {

    public static void main(String[] args) {
        Pippo pippo = new Pippo();
        pippo.getApplication().GET("/", ContactsController.class, "index");
        pippo.start();
    }

}
```

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
- RouteHandlerChain

If you want to extend Pippo (create new module, modify some default behaviors) you would need to know about:
- PippoFilter
- RouteMatcher
- WebServer
- TemplateEngine
- ServiceLocator
- Initializer

The easy mode to run your application si to use Pippo wrapper class.  

__Route__ are URL schema, which describe the interfaces for making requests to your web application. Combining an HTTP request method (a.k.a. HTTP verb) and a path pattern, you define URLs in your application.  
Each route has an associated __RouteHandler__, which does the job of performing any action in the application and sending the HTTP response.  
Routes are defined using an HTTP verb and a path pattern. Any request to the server that matches a route definition is routed to the associated route handler.

```java
GET("/", new RouteHandler() {

    @Override
    public void handle(Request request, Response response, RouteHandlerChain chain) {
        response.send("Hello World");
    }

});

// or more concise using Java 8 lambdas

GET("/", (request, response, chain) -> response.send("Hello World"));
```

Routes in Pippo are created using methods named after HTTP verbs. For instance, in the previous example, we created a route to handle GET requests to the root of the website. You have a corresponding method in Application for all commonly used HTTP verbs (GET, POST, DELETE, HEAD, PUT). For a basic website, only GET and POST are likely to be used.

The route that is defined first takes precedence over other matching routes. So the ordering of routes is crucial to the behavior of an application.   

Each defined route has an __urlPattern__.
The route can be static or dynamic:
- static ("/", "/hello", "/contacts/1")
- dynamic (regex: "/.*" or parameterized: "/contact/{id}", "/contact/{id: [0-9]+}")

As you can see, it's easy to create routes with parameters. A parameter is wrapped by curly braces `{name}` and can optionally specify a regular expression. 

You can retrieve the path parameter value for a request in type safe mode using:

```java
GET("/contact/{id: [0-9]+}", (request, response, chain) -> {
    int id = request.getParameter("id").toInt(0);    
    String action = request.getParameter("action").toString("new");
    
    Map<String, Object> model = new HashMap<>();
    model.put("id", id);
    model.put("action", action)
    response.render("crud/contact", model);
});
```

The __Response__ is a wrapper over HttpServletResponse from servlet API and it provides functionality for modifying the response. You can send a char sequence with `send` method, or a file with `file` method, or a json with `json` method. Also you can send a template file merged with a model using `render` method.  

The __Request__ is a wrapper over HttpServletRequest from servlet API.  

When a request is made to the server, which matches a route definition, the associated handlers are called. The __RouteMather__ contains a method `List<RouteMatch> findRoutes(String requestMethod, String requestUri)` that returns all routes which matches a route definition (String requestMethod, String requestUri).  
Why does RouterMatcher have the method findRoutes(...):List<RouteMatch> instead of findRoute(...):RouteMatch? My response is that I want to use the RouteHandler also to define the Filter concept. I don't want to define a new interface Filter with the same signature as the RouteHandler interface.  
A __RouteHandler__ has only one method `void handle(Request request, Response response, RouteHandlerChain chain)`. The __handle__ method can be an endpoint or not. A regular RouteHandler is an endpoint, that means that the response is committed in the handle method of that RouteHandler instance. A committed response has already had its status code and headers written. In Response class exists a method `isCommitted()` that tell you if the response is committed or not. The methods from Response that commit a response are: `send`, `json`, `file`, `render`. If you try to commit a response that was already committed (after content has been written) than a PippoRuntimeException will be thrown.  
You can reconize in a very simple mode if a response method sets the committed flag on true (it's an endpoint method) just by looking at its signature. The convension is that these methods return `void` unlike the other methods that return `Response` (you can set many fields at once).  
You can see a filter as a RouteHandler that does not commit the response. A filter is typically used to perform a particular piece of functionality either before or after the primary functionality (another RouteHandler) of a web application is performed. The filter might determine that the user does not have permissions to access a particular servlet, and it might send the user to an error page rather than to the requested resource.  

```java
// audit filter
GET("/.*", (request, response, chain) -> {
    System.out.println("Url: '" + request.getUrl());
    System.out.println("Uri: '" + request.getUri());
    System.out.println("Parameters: " + request.getParameters());
});

GET("/hello",(request, response, chain) -> response.send("Hello World"));
```

You can see in the above example that I put an audit filter in front of all requests.

From version 0.4, Pippo comes with a new very useful method `getEntityFromParameters` in __Request__. This method binding the request parameters values from PUT/POST/etc to Java objects (POJOs).  
Let's see some code that shows in action this feature:
```java
POST("/contact", (request, response, chain) -> {
    String action = request.getParameter("action").toString();
    if ("save".equals(action)) {
        Contact contact = request.getEntityFromParameters(Contact.class);
        contactService.save(contact);
        response.redirect("/contacts");
    }
});
```
The old version has:
```java
POST("/contact", (request, response, chain) -> {
    String action = request.getParameter("action").toString();
    if ("save".equals(action)) {
        Contact contact = new Contact();
        contact.setId(request.getParameter("id").toInt(-1));
        contact.setName(request.getParameter("name").toString());
        contact.setPhone(request.getParameter("phone").toString());
        contact.setAddress(request.getParameter("address").toString());
        contactService.save(contact);
        response.redirect("/contacts");
    }
});                    
```

For now, Pippo comes with a constraint. The types for entity's fields that will be binding must be String, Boolean, Integer, Long, Float or Double.  

An __Application__ is a class which associates with an instance of PippoFilter to serve pages over the HTTP protocol. Usually I subclass this class and add my routes in `init()` method.

```java
public class MyDemo {

    public static void main(String[] args) {
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

I want to point that the static files/resources are served directly by the embedded server (in case of pippo-jetty) and not by Pippo (through PippoFilter). 

Reverse routing
-------------------
Reverse routing is a feature in Pippo that is used to allow you to easily change your URL structure without having to modify all your code.  
Why would you want to build URLs instead of hard-coding them into your templates?  
One answer is that reversing is often more descriptive than hard-coding the URLs. More importantly, it allows you to change URLs in one go, without having to remember to change URLs all over the place.  

If you create a route like (the Controller aproach):
```java
GET("/contacts/{id}", ContactsController.class, "show");
```

This route will take requests such as `/contacts/1` and map it to the __show__ method on the __Contacts__ controller.

Using reverse routing we can create a link to it and pass in any parameters that we have defined. Extra parameters suplied in method urlFor are added as query parameters in the generated link.
```java
Map<String, Object> parameters = new HashMap<>();
parameters.put("id", 1);
parameters.put("action", "new"); // extra parameter
String url = getApplication().urlFor(ContactsController.class, "show", parameters);
```
And now the link created should be something like `/contacts/1?action=new`.

The same can be applied to non controller routes:
```java
GET("/contacts/{id}", (request, response, chain) -> {...}
```

Now we can use `Application.urlFor(String urlPattern, Map<String, Object> parameters)` method to retrieves the URL:
```java
Map<String, Object> parameters = new HashMap<>();
parameters.put("id", 1);
parameters.put("action", "new");
String url = getApplication().urlFor("/contacts/:id", parameters);
```

In conclusion if you want to create links to routes or controllers you must use `Application.urlFor` methods.

Locals
-------------------
Locals are good for storing variables for the __CURRENT__ request/response cycle.
These variables will be available automatically to all templates for the current request/response cycle.

```java
GET("/contacts", (request, response, chain) -> {
    /*
    // variant 1 (with model)
    Map<String, Object> model = new HashMap<>();
    model.put("contacts", contactService.getContacts());
    response.render("crud/contacts", model);
    */

    // variant 2 (with locals)
    response.getLocals().put("contacts", contactService.getContacts());
    response.render("crud/contacts");
```

Another scenario for locals:
```java
// filter that inject 'contacts' in locals and implicit in all templates' model
GET("/contact*", (request, response, chain) -> {
    response.getLocals().put("contacts", contactService.getContacts());
});

// just consume 'contacts' in template 
GET("/contact.*", (request, response, chain) -> {
    response.render("crud/contacts");
});
```

The snippet for contacts (show a list with all contacts' name):
```html
<html>
    <body>
        <ul>
        <#list contacts as contact>
            <li>${contact.name}</li>
        </#list>
        </ul?
    </body>
</html>
```

Upload
-------------------
Pippo has builtin support for upload. For a perfect running example see UploadDemo from pippo-demo module.    

In what follows I will show you how simple it is to work with uploads.

```java
public static void main(String[] args) {
    Pippo pippo = new Pippo();
    Application application = pippo.getApplication();
    // the following two lines are optional 
    application.setUploadLocation("upload");
    application.setMaximumUploadSize(100 * 1024); // 100k

    application.GET("/", (request, response, chain) -> response.render("upload"));

    application.POST("/upload", (request, response, chain) -> {
        // retrieves the value for 'file' input
        FileItem file = request.getFile("file");
        try {
            // write to disk
            //file.write(file.getSubmittedFileName()); // write the file in application upload location
            File uploadedFile = new File(file.getSubmittedFileName());
            file.write(uploadedFile);

            // send response
            response.send("Uploaded file to '" + uploadedFile + "'");
        } catch (IOException e) {
            throw new PippoRuntimeException(e); // to display the error stack as response
        }
    });

    pippo.start();
}
```

The content for 'upload' is:
```html
<html>
    <head>
        <title>Welcome!</title>
    </head>
    <body>
        <form action="/upload" method="post" enctype="multipart/form-data">
            <input type="file" name="file">
            <input type="submit" value="Submit">
        </form>
    </body>
</html>
```

Security
-------------------
You can secure your application or only some parts using a filter (a RouteHandler). Remember that routes are matched 
in the order they are added/defined so put your security filter in front of regular routes (regular routes are 
endpoint routes for a request).

I will show you a simple implementation for a security filter.

```java
// authentication filter
GET("/contact.*", (request, response, chain) -> {
    if (request.getSession().getAttribute("username") == null) {
        request.getSession().setAttribute("originalDestination", request.getUri());
        response.redirect("/login");
    } else {
        chain.next();
    }
});

// show contacts page
GET("/contacts", (request, response, chain) -> response.send("contacts"));

// show contact page for the contact with id specified as path parameter 
GET("/contact/{id}", (request, response, chain) -> response.send("contact"));

// show login page
GET("/login", request, response, chain) -> {
    Map<String, Object> model = new HashMap<>();
    String error = (String) request.getSession().getAttribute("error");
    request.getSession().removeAttribute("error");
    if (error != null) {
        model.put("error", error);
    }
    response.render("crud/login", model);
});

// process login submit
POST("/login", (request, response, chain) -> {
    String username = request.getParameter("username").toString();
    String password = request.getParameter("password").toString();
    if (authenticate(username, password)) {
        request.getSession().setAttribute("username", username);
        String originalDestination = (String) request.getSession().getAttribute("originalDestination");
        response.redirect(originalDestination != null ? originalDestination : "/contacts");
    } else {
        request.getSession().setAttribute("error", "Authentication failed");
        response.redirect("/login");
    }
});

// a dump implementation for authenticate method
private boolean authenticate(String username, String password) {
    return !username.isEmpty() && !password.isEmpty();
}
```

The content for login can be:
```html
<html>
    <head>
        <title>Login</title>
    </head>
    <body>
        <#if error??>
            ${error}
        </#if>
        
        <form method="post" action="/login">
            <input placeholder="Username" name="username">
            <input placeholder="Password" name="password" type="password">
            <input type="submit" value="Login">
        </form>
    </body
</html>
```

In above code I want to protect all pages (contacts, contact) for the Contact domain entity.  
The authentication tests to see if the 'username' attribute is present in the session object. If 'username' is present
than call the regular route with `chain.next()` else redirect to the login page. I added 'originalDestination' attribute
because after authentication process I want to continue with the original destination (original url). 

Settings
--------
Your application and modules can be configured using a simple `application.properties` text file. This file supports
mode-sensitive keys and recursive includes.

    # Include files serve as base settings which may be overridden in this file.
    include = /path/to/include.properties, /path/to/other.properties
    
    application.name = Default Pippo Application
    %dev.application.name = Developing Pippo Application
    %test.application.name = Testing Pippo Application
    %prod.application.name = My Pippo Application

If a setting provides a mode-specific variant for the current runtime mode it's value will be used.  Otherwise, the default
setting's value will be used.

There is some flexibility in specifying from where to load your `application.properties` file. This is the order Pippo will
use to try to resolve an `application.properties` file.

1. `-Dpippo.settings=/path/to/application.properties` *(flexible external location)*
2. `$user.dir/application.properties` *(the working directory of your application)*
3. `conf/application.properties` *(an embedded classpath resource)*

An externally located `application.properties` file is automatically reloaded, if modified, on the next setting access
within Pippo. This allows your Pippo application to be responsive without being restarted.

Embedded web server
-------------------
Most server-side Java applications (e.g. web or service-oriented) are intended to run within a container. 
The traditional way to package these apps for distribution is to bundle them as a WAR file. 
Of course you can use the above model for your application development or you can use the simple way. 
Rather than your application being deployed to a container, an embedded container is deployed within the application itself.
Pippo comes with Jetty as embedded web server. You can choose another container if you want (for example Tomcat).
 
See below the classic `Hello World` in Pippo using the default web server:
```java
public class HelloWorld {

    public static void main(String[] args) {
        Pippo pippo = new Pippo();
        pippo.getApplication.GET("/", (request, response, chain) -> response.send("Hello World!"));
        pippo.start();
    }

}
```

You can run _HelloWorld_ class from your IDE (or command line) as a normal (desktop) application. 
The `default port` for embedded web server is __8338__ so open your internet browser and type `http://localhost:8338` to 
see the result.

You can change some aspects of the embedded web server using `WebServerSettings`:
```java
Pippo pippo = new Pippo();
pippo.getServer().getSettings().port(8081).staticFilesLocation("/public");
pippo.start();
```

In above snippet I changed the port to _8081_ and ai specify the static file location to _public_.

If you need to create support for another embedded web server that is not implemented in Pippo or third-party modules 
than all you need to do is to implement `WebServer` (or to extends `AbstractWebServer`).

```java
public interface WebServer {

    public WebServerSettings getSettings();

    public void setSettings(WebServerSettings settings);

    public PippoFilter getPippoFilter();

    public void setPippoFilter(PippoFilter pippoFilter);

    public void start();

    public void stop();
    
}
```

If you want to make your embedded server plugable for Pippo than you must add file 
`ro.fortsoft.pippo.core.WebServer` in _src/main/resources/META-INF/services_ folder with your class name that implements  
WebServer as content (for an hypothetical Tomcat integration the content file should be _ro.fortsoft.pippo.tomcat.TomcatServer_).

Templates
-------------------
Not all applications are REST based and you might need to generate some HTML. 
It is not productive to inline the HTML in strings in your code and concatenate them at request time. 
Pippo ships with Freemarker template engine as default and Jade template engine as a builtin alternative. These engines
are optional and Pippo detect automatically the template engine using __ServiceLocator__.  
You can set programmatically the desired template engine using `setTemplateEngine(TemplateEngine templateEngine)` from
__Appplication__.

If you want to add support for other template engine in your application, please create a new module/project, add file 
`ro.fortsoft.pippo.core.TemplateEngine` in _src/main/resources/META-INF/services_ folder with your class name that implements 
TemplateEngine as content (for Jade the content file is _ro.fortsoft.pippo.jade.JadeTemplateEngine_).  

The `TemplateEngine` interface contains only one method, `public void render(String templateName, Map<String, Object> model, Writer writer)`, 
that must be implemented by your concrete template engine.

The template engine is uses in `public void render(String templateName, Map<String, Object> model)` and `public void render(String templateName)` 
from `Response` class.

Bellow is a code snippet about how you can use a template as response to a request:
```java
GET("/contact/{id}", (request, response, chain) -> {
    int id = request.getParameter("id").toInt(0);    
    String action = request.getParameter("action").toString("new");
    
    Map<String, Object> model = new HashMap<>();
    model.put("id", id);
    model.put("action", action)
    response.render("crud/contact", model);
});
```

Don't forget that `locals` variables from a response will be available automatically to all templates for the current request/response cycle.
 
For each template engine we expose its configuration. For example __Freemarker__ works with `freemarker.template.Configuration` and __Jade__ works with `de.neuland.jade4j.JadeConfiguration`.  
In _Application.init_ you can create a new instance for a discovered template engine or you can modify its configuration.

```java
public class CrudApplication extends Application {

   @Override
   public void init() {
      super.init();

      FreemarkerTemplateEngine templateEngine = new FreemarkerTemplateEngine();
      try {
         templateEngine.getConfiguration().setDirectoryForTemplateLoading(new File("src/main/resources/templates/"));
      } catch (IOException e) {
         throw new PippoRuntimeException(e);
      }
      setTemplateEngine(templateEngine);

      // ... add routes
   }

}
```

or
```java
public class CrudApplication extends Application {

   @Override
   public void init() {
      super.init();

      Configuration configuration = ((FreemarkerTemplateEngine) templateEngine).getConfiguration();
      try {
         configuration.setDirectoryForTemplateLoading(new File("src/main/resources/templates/"));
      } catch (IOException e) {
         throw new PippoRuntimeException(e);
      }

      // ... add routes
   }

}
```

For more information about how to implement a template engine please see _pippo-freemarker_ and _pippo-jade_ modules.

Spring IoC
-------------------
Pippo can be used together with the Spring framework, using Spring as a dependency injection container. When Pippo creates new instances of your various Controller subclasses, the pippo-spring integration would then take care that the Spring-managed service beans (e.g. Services) get injected into the desired instance fields (marked by the `@Inject` annotations in your code). An example of such a Controller subclass could look as follows:

```java
public class ContactsController extends Controller {

    @Inject
    private ContactService contactService;

    public void index() {
        getResponse().getLocals().put("contacts", contactService.getContacts());
        getResponse().render("crud/contacts");
    }

}
```

Pippo automatically creates the ContactsController instance and pippo-spring integration injects the ContactService service bean, so basically you don’t have to worry about any of that stuff yourself. 

To activate pippo-spring integration in your Application you must add `SpringControllerInjector`:
```java
public class MyApplication extends Application {

    @Override
    public void init() {
        super.init();

        // create spring application context
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        
        // registering SpringControllerInjector
        getControllerInstantiationListeners().add(new SpringControllerInjector(applicationContext));

        // add controller
        GET("/", ContactsController.class, "index");        
    }

}
```

where SpringConfiguration can looks like:
```java
@Configuration
public class SpringConfiguration {

    @Bean
    public ContactService contactService() {
        return new InMemoryContactService();
    }

}
```

Modularity
-------------------
Pippo was designed since the first version with the modularity in mind. Any aspect of this framework can be changed:
- embedded `WebServer` (using _Pippo setServer_ or auto discovery mechanism)
- `TemplateEngine` (using _Application.setTemplateEngine_ or auto discovery mechanism)
- `RouteMatcher` (using _Application.setRouteMatcher_)
- `ExceptionHandler` (using _Application.setExceptionHandler_)
- `RouteNotFoundHandler` (using _Application.setRouteNotFoundHandler_)

Also you can set some parameters related to file upload process (_Application.setUploadLocation_ and _Application.setMaximumUploadSize_).
You can modify some settings for an embedded WebServer using _WebServerSettings_.

We chose Service Loader mechanism from Java as builtin module system in Pippo because is a standard easy to use mechanism
You can create a modular application using `ServiceLocator` class (trivial wrapper over Service Loader concept).

To improve the modularity mechanism, we added the concept of `Initializer`.  
When Pippo starts up an application, it scans the classpath roots, looking for files named `pippo.properties`. It reads 
every pippo.properties file it finds, and it instantiates and execute the initializers defined in those files. 

To demonstrate the initializer concept I added a dump _FreemarkerInitializer_ in pippo-freemarker module. In our example, 
the _pippo.properties_ file (which should be packaged in the root of the classpath) contains only one line:
```properties
initializer=ro.fortsoft.pippo.freemarker.FreemarkerInitializer
```

The initializer can be implemented like this:
 ```java
public class FreemarkerInitializer implements Initializer {

    @Override
    public void init(Application application) {
        application.setTemplateEngine(new FreemarkerTemplateEngine());
        // or do some freemarker configuration
    }

    @Override
    public void destroy(Application application) {
        // do nothing    
    }

} 
 ```

One scenario when I can use the _Initializer_ concept is when I split my application in several modules and each module 
wants to add some routes to the application.
For example my application comes with two modules (two jars): _contacts_ and _users_.
I can have _ContactInitializer.java_ with this content:
```java
public class ContactInitializer implements Initializer {

    @Override
    public void init(Application application) {
        // show contacts page
        application.GET("/contacts", (request, response, chain) -> response.send("contacts"));
        
        // show contact page for the contact with id specified as path parameter 
        application.GET("/contact/{id}", (request, response, chain) -> response.send("contact"));
    }

    @Override
    public void destroy(Application application) {
        // do nothing
    }

}
```

I can have _UserInitializer.java_ with this content:
```java
public class ContactInitializer implements Initializer {

    @Override
    public void init(Application application) {
        // show users page
        application.GET("/users", (request, response, chain) -> response.send("users"));
        
        // show user page for the user with id specified as path parameter 
        application.GET("/user/{id}", (request, response, chain) -> response.send("user"));
    }

    @Override
    public void destroy(Application application) {
        // do nothing
    }

}
```

Runtime mode
-------------------
An application can run in three modes: __DEV__(development), __TEST__(testing) and __PROD__(production).

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
The demo application is in pippo-demo module. The demo module contains some demo applications: 
- General
     - Simple
     - Crud
     - Upload
     - Validation (based on JSR 303 and Hibernate Validator)
- Template Engine
     - Groovy
     - Freemarker
     - Jade
     - Pebble
     - Trimou
- IoC/DI
     - Guice
     - Spring
  
CrudDemo is a <b>C</b>reate <b>R</b>etrieve <b>U</b>pdate <b>D</b>elete demo (with twitter bootstrap as static resources). 
    
Mailing list
--------------
Much of the conversation between developers and users is managed through [mailing list] (http://groups.google.com/group/pippo-java).

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
