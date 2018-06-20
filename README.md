<img src="pippo-logo.svg" width="250"/>

Micro Java Web Framework
=====================
[![Join the chat at https://gitter.im/decebals/pippo](https://badges.gitter.im/decebals/pippo.svg)](https://gitter.im/decebals/pippo?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Travis CI Build Status](https://travis-ci.org/pippo-java/pippo.png)](https://travis-ci.org/pippo-java/pippo)
[![Coverage Status](https://coveralls.io/repos/pippo-java/pippo/badge.svg?branch=master&service=github)](https://coveralls.io/github/pippo-java/pippo?branch=master)
[![Maven Central](http://img.shields.io/maven-central/v/ro.pippo/pippo.svg)](http://search.maven.org/#search|ga|1|ro.pippo)

<!--
[![Issue Stats](http://www.issuestats.com/github/pippo-java/pippo/badge/issue?style=flat)](http://www.issuestats.com/github/pippo-java/pippo)
[![Issue Stats](http://www.issuestats.com/github/pippo-java/pippo/badge/pr?style=flat)](http://www.issuestats.com/github/pippo-java/pippo)
-->


It's an open source (Apache License) micro web framework in Java, with minimal dependencies and a quick learning curve.     
The goal of this project is to create a micro web framework in Java that should be easy to use and hack.      
The size of `pippo-core` is only __140 KB__ and the size of `pippo-controller` (optional) is only __45 KB__.

Sample code
---------------

#### 1. Routes approach

First we must create an __Application__ and add some routes:  

```java
public class BasicApplication extends Application {

    @Override
    protected void onInit() {
        // send 'Hello World' as response
        GET("/", routeContext -> routeContext.send("Hello World"));

        // send a file as response
        GET("/file", routeContext -> routeContext.send(new File("pom.xml")));

        // send a json as response
        GET("/json", routeContext -> {
            Contact contact = createContact();
            routeContext.json().send(contact);
        });

        // send xml as response
        GET("/xml", routeContext -> {
            Contact contact = createContact();
            routeContext.xml().send(contact);
        });

        // send an object and negotiate the Response content-type, default to XML
        GET("/negotiate", routeContext -> {
            Contact contact = createContact();
            routeContext.xml().negotiateContentType().send(contact);
        });

        // send a template with name "hello" as response
        GET("/template", routeContext -> {
            routeContext.setLocal("greeting", "Hello");
            routeContext.render("hello");
        });
    }

    private Contact createContact() {
        return new Contact()
            .setId(12345)
            .setName("John")
            .setPhone("0733434435")
            .setAddress("Sunflower Street, No. 6");
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

    // getters and setters

}
```

The second step is to choose your favorite [server](http://www.pippo.ro/doc/server.html),
[template engine](http://www.pippo.ro/doc/templates.html)
and [content type engine](http://www.pippo.ro/doc/content-types.html).  
For example, I will choose `Jetty` as server, `Freemarker` as template engine, `Jackson` as JSON engine and `JAXB` as XML engine.  
My Maven `pom.xml` looks like:

```xml
<dependency>
    <groupId>ro.pippo</groupId>
    <artifactId>pippo-core</artifactId>
    <version>${pippo.version}</version>
</dependency>

<dependency>
    <groupId>ro.pippo</groupId>
    <artifactId>pippo-jetty</artifactId>
    <version>${pippo.version}</version>
</dependency>

<dependency>
    <groupId>ro.pippo</groupId>
    <artifactId>pippo-freemarker</artifactId>
    <version>${pippo.version}</version>
</dependency>

<dependency>
    <groupId>ro.pippo</groupId>
    <artifactId>pippo-jackson</artifactId>
    <version>${pippo.version}</version>
</dependency>
```

The last step it's to start __Pippo__ with your application as parameter:

```java
public class BasicDemo {

    public static void main(String[] args) {
        Pippo pippo = new Pippo(new BasicApplication());
        pippo.start();
    }

}
```

Pippo launches the embedded web server (found in your classpath) and makes the application available on port `8338` (default value).
Open your internet browser and check the routes declared in Application:

 - `http://localhost:8338`
 - `http://localhost:8338/file`
 - `http://localhost:8338/json`
 - `http://localhost:8338/xml`
 - `http://localhost:8338/negotiate`
 - `http://localhost:8338/template`

#### 2. Controllers approach

Define controller(s):

```java
@Path("/contacts")
@Logging
public class ContactsController extends Controller {

    private ContactService contactService;

    public ContactsController() {
        contactService = new InMemoryContactService();
    }

    @GET
    @Named("index")
//    @Produces(Produces.HTML)
    @Metered
    @Logging
    public void index() {
        // inject "user" attribute in session
        getRouteContext().setSession("user", "decebal");

        // send a template with name "contacts" as response
        getResponse()
            .bind("contacts", contactService.getContacts())
            .render("contacts");
    }

    @GET("/uriFor/{id: [0-9]+}")
    @Named("uriFor")
    @Produces(Produces.TEXT)
    @Timed
    public String uriFor(@Param int id, @Header String host, @Session String user) {
        System.out.println("id = " + id);
        System.out.println("host = " + host);
        System.out.println("user = " + user);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);

        String uri = getApplication().getRouter().uriFor("api.get", parameters);

        return "id = " + id + "; uri = " + uri;
    }

    @GET("/api")
    @Named("api.getAll")
    @Produces(Produces.JSON)
    @NoCache
    public List<Contact> getAll() {
        return contactService.getContacts();
    }

    @GET("/api/{id: [0-9]+}")
    @Named("api.get")
    @Produces(Produces.JSON)
    public Contact get(@Param int id) {
        return contactService.getContact(id);
    }

}
```

```java
@Path("/files")
public class FilesController extends Controller {

    @GET
    public void index() {
        // send a template with name "files" as response
        getRouteContext().render("files");
    }

    @GET("/download")
    public File download() {
        // send a file as response
        return new File("pom.xml");
    }

    @POST("/upload")
    @Produces(Produces.TEXT)
    public String upload(FileItem file) {
        // send a text (the info about uploaded file) as response
//        return file.toString();
        return new StringBuilder()
            .append(file.getName()).append("\n")
            .append(file.getSubmittedFileName()).append("\n")
            .append(file.getSize()).append("\n")
            .append(file.getContentType())
            .toString();
    }

}
```

Add controller(s) in your application:

```java
public class BasicApplication extends ControllerApplication {

    @Override
    protected void onInit() {
        addControllers(ContactsController.class); // one instance for EACH request
        // OR
        addControllers(new ContactsController()); // one instance for ALL requests

        addControllers(FilesController.class);
    }

}
```

Don't forget that the `Controller` concept is included in `pippo-controller` module so you must add this module as dependency in your project.

Documentation
---------------
Documentation is available on [pippo.ro](http://www.pippo.ro)

Demo
---------------
Demo applications are available on [pippo-demo](https://github.com/pippo-java/pippo-demo)   
For a real life application built with Pippo please look at [Web Accounting - Pippo Demo](https://github.com/pippo-java/matilda)
