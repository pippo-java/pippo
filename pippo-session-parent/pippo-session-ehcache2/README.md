Pippo Session Ehcache2
=====================
[Ehcache2](http://www.ehcache.org/) Session Manager integration for [Pippo](http://www.pippo.ro/)

Sample code
---------------

First we must create the ehcache configuration file: `src/main/resources/ehcache.xml`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:noNamespaceSchemaLocation="ehcache.xsd"
         updateCheck="true"
         monitoring="autodetect"
         dynamicConfig="true">

    <!-- By default, Ehcache stored the cached files in temp folder. -->
    <diskStore path="java.io.tmpdir" />
    
    <!-- timeToIdleSeconds = 300 seconds = 5 minutes -->
    <cache name="session" 
           maxEntriesLocalHeap="100"
           maxEntriesLocalDisk="200" 
           eternal="false" 
           diskSpoolBufferSizeMB="20"
           timeToIdleSeconds="300"
           timeToLiveSeconds="0"
           memoryStoreEvictionPolicy="LFU" 
           transactionalMode="off">
        <persistence strategy="localTempSwap" />
    </cache>

</ehcache>
```

Add the following code sniped in your application:

```java
public class MyApplication extends Application {

    @Override
    protected void onInit() {
        // add routes here
    }

    @Override
    protected RequestResponseFactory createRequestResponseFactory() {
        SessionDataStorage sessionDataStorage = new EhcacheSessionDataStorage();
        SessionManager sessionManager = new SessionManager(sessionDataStorage);

        return new SessionRequestResponseFactory(this, sessionManager);
    }

    @Override
    protected void onDestroy() {
        CacheManager.getInstance().shutdown();
    }
}
```
