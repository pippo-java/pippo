Pippo Session Infinispan
=====================
[Infinispan](http://infinispan.org/) Session Manager integration for [Pippo](http://www.pippo.ro/)

Sample code
---------------

First we must create the infinispan configuration file: `src/main/resources/infinispan.xml`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<infinispan
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="urn:infinispan:config:8.2 http://www.infinispan.org/schemas/infinispan-config-8.2.xsd"
    xmlns="urn:infinispan:config:8.2">

    <cache-container default-cache="default">
        <local-cache name="session">
            <!-- Idle time in milliseconds = 300000 milliseconds = 5 minutes -->
            <expiration max-idle="300000" />
        </local-cache>
    </cache-container>

</infinispan>
```

Add the following code sniped in your application:

```java
public class MyApplication extends Application {

    private EmbeddedCacheManager cacheManager;

    @Override
    protected void onInit() {
        this.cacheManager = InfinispanFactory.create();
        // add routes here
    }

    @Override
    protected RequestResponseFactory createRequestResponseFactory() {
        SessionDataStorage sessionDataStorage = new InfinispanSessionDataStorage(this.cacheManager);
        SessionManager sessionManager = new SessionManager(sessionDataStorage);

        return new SessionRequestResponseFactory(this, sessionManager);
    }

    @Override
    protected void onDestroy() {
        this.cacheManager.stop();
    }
}
```
