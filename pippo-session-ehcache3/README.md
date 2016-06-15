Pippo Session Ehcache3
=====================
[Ehcache3](http://www.ehcache.org/) Session Manager integration for [Pippo](http://www.pippo.ro/)

Sample code
---------------

First we must create the ehcache configuration file: `src/main/resources/ehcache.xml`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ehcache:config
    xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
    xmlns:ehcache="http://www.ehcache.org/v3"
    xmlns:jcache="http://www.ehcache.org/v3/jsr107">
    
    <ehcache:cache alias="session">
        <ehcache:key-type>java.lang.String</ehcache:key-type>
        <ehcache:value-type>ro.pippo.session.SessionData</ehcache:value-type>
        <ehcache:expiry>
            <ehcache:ttl unit="minutes">5</ehcache:ttl>
        </ehcache:expiry>
        <ehcache:resources>
            <ehcache:heap unit="MB">1</ehcache:heap>
        </ehcache:resources>
    </ehcache:cache>

</ehcache:config>
```

Add the following code sniped in your application:

```java
public class MyApplication extends Application {

    private CacheManager cacheManager;

    @Override
    protected void onInit() {
        this.cacheManager = EhcacheFactory.create();
        // add routes here
    }

    @Override
    protected RequestResponseFactory createRequestResponseFactory() {
        SessionDataStorage sessionDataStorage = new EhcacheSessionDataStorage(this.cacheManager);
        SessionManager sessionManager = new SessionManager(sessionDataStorage);

        return new SessionRequestResponseFactory(this, sessionManager);
    }

    @Override
    protected void onDestroy() {
        this.cacheManager.close();
    }
}
```
