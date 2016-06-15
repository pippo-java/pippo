Pippo Session Xmemcached
=====================
[Xmemcached](https://github.com/killme2008/xmemcached) Session Manager integration for [Pippo](http://www.pippo.ro/)

Sample code
---------------
First we must add in `src/main/resources/conf/application.properties`:

```
# whitespace separated host or IP addresses and port numbers of the form "host:port host2:port"
memcached.hosts = localhost:11211
# Optional
memcached.user = username
memcached.password = password
# BINARY or TEXT
memcached.protocol = BINARY
# PLAIN and/or CRAM-MD5 (comma separated)
memcached.authMechanisms = PLAIN
```

Add the following code sniped in your application:

```java
public class MyApplication extends Application {

    private MemcachedClient client;

    @Override
    protected void onInit() {
        this.client = XmemcachedFactory.create(getPippoSettings());
        // add routes here
    }

    @Override
    protected RequestResponseFactory createRequestResponseFactory() {
        SessionDataStorage sessionDataStorage = new XmemcachedSessionDataStorage(this.client);
        SessionManager sessionManager = new SessionManager(sessionDataStorage);

        return new SessionRequestResponseFactory(this, sessionManager);
    }

    @Override
    protected void onDestroy() {
        try {
            this.client.shutdown();
        } catch (IOException ex) {
            // do something
        }
    }
}
```
