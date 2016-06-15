Pippo Session Hazelcast
=====================
[Hazelcast](http://hazelcast.org/) Session Manager integration for [Pippo](http://www.pippo.ro/)

Sample code
---------------

First we must create the Hanzelcast configuration file: `src/main/resources/hazelcast.xml`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<hazelcast xsi:schemaLocation="http://www.hazelcast.com/schema/config
    http://www.hazelcast.com/schema/config/hazelcast-config-3.0.xsd"
           xmlns="http://www.hazelcast.com/schema/config"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <properties>
        <property name="hazelcast.logging.type">slf4j</property>
    </properties>
    
    <!-- time-to-live-seconds = 300 seconds = 5 minutes -->
    <map name="session">
        <time-to-live-seconds>300</time-to-live-seconds>
        <max-idle-seconds>0</max-idle-seconds>
        <eviction-policy>LRU</eviction-policy>
    </map>

</hazelcast>
```

Add the following code sniped in your application:

```java
public class MyApplication extends Application {

    private HazelcastInstance instance;

    @Override
    protected void onInit() {
        this.instance = Hazelcast.newHazelcastInstance();
        // add routes here
    }

    @Override
    protected RequestResponseFactory createRequestResponseFactory() {
        SessionDataStorage sessionDataStorage = new HazelcastSessionDataStorage(this.instance);
        SessionManager sessionManager = new SessionManager(sessionDataStorage);

        return new SessionRequestResponseFactory(this, sessionManager);
    }

    @Override
    protected void onDestroy() {
        this.instance.shutdown();
    }

}
```

Map Options
---------------
For more map options to configure the session, you can consult [Hazelcast](http://docs.hazelcast.org/docs/3.6/manual/html-single/index.html#evicting-map-entries)
