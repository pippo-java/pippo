Pippo Session Jedis
=====================
[Jedis](https://github.com/xetorthio/jedis) Session Manager integration for [Pippo](http://www.pippo.ro/)

Sample code
---------------
First we must add in `src/main/resources/conf/application.properties`:

```
# Host of the form "redis://[:password@]host[:port][/db-number][?option=value]"
redis.host = redis://localhost:6379
# Optional
# redis.minIdle = 0
# redis.maxIdle = 8
# redis.maxTotal = 8
```

Add the following code sniped in your application:

```java
public class MyApplication extends Application {

    private JedisPool client;

    @Override
    protected void onInit() {
        this.client = JedisFactory.create(getPippoSettings());
        // add routes here
    }

    @Override
    protected RequestResponseFactory createRequestResponseFactory() {
        SessionDataStorage sessionDataStorage = new JedisSessionDataStorage(this.client);
        SessionManager sessionManager = new SessionManager(sessionDataStorage);

        return new SessionRequestResponseFactory(this, sessionManager);
    }

    @Override
    protected void onDestroy() {
        this.client.destroy();
    }
}
```
