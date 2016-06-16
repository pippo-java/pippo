Pippo Session JCache
=====================
[JCache (JSR 107)](https://jcp.org/en/jsr/detail?id=107) Session Manager integration for [Pippo](http://www.pippo.ro/)

Sample code
---------------

Add the following code sniped in your application:

```java
public class MyApplication extends Application {

    @Override
    protected void onInit() {
        // add routes here
    }

    @Override
    protected RequestResponseFactory createRequestResponseFactory() {
        SessionDataStorage sessionDataStorage = new JCacheSessionDataStorage();
        SessionManager sessionManager = new SessionManager(sessionDataStorage);

        return new SessionRequestResponseFactory(this, sessionManager);
    }

    @Override
    protected void onDestroy() {
        Caching.getCachingProvider().getCacheManager().close();
    }

}
```

JCache Implementations
---------------

JCache defines the interfaces which of course are implemented by different vendors a.k.a Providers.

Some providers are:

* [Hazelcast](http://docs.hazelcast.org/docs/3.6/manual/html-single/index.html#hazelcast-jcache)
```xml
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast</artifactId>
    <version>${last.version}</version>
</dependency>
```
* [Ehcache](http://www.ehcache.org/documentation/3.0/107.html)
```xml
<dependency>
    <groupId>org.ehcache</groupId>
    <artifactId>ehcache</artifactId>
    <version>${last.version}</version>
</dependency>
```
* [Infinispan](http://infinispan.org/docs/8.2.x/user_guide/user_guide.html#_use_jcache_caching_annotations)
```xml
<dependency>
    <groupId>org.infinispan</groupId>
    <artifactId>infinispan-jcache</artifactId>
    <version>${last.version}</version>
</dependency>
```
* [Ignite](https://apacheignite.readme.io/docs/jcache)
```xml
<dependency>
    <groupId>org.apache.ignite</groupId>
    <artifactId>ignite-core</artifactId>
    <version>${last.version}</version>
</dependency>
```
* [Oracle Coherence](https://www.oracle.com/middleware/coherence/index.html)
* [Reference Implementation](https://github.com/jsr107/RI) – this is more for **reference** purpose rather than a production quality implementation. It is per the specification though and you can be rest assured of the fact that it does in fact pass the TCK as well
