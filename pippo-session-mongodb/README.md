Pippo Session MongoDB
=====================
[MongoDB](https://mongodb.github.io/mongo-java-driver/) Session Manager integration for [Pippo](http://www.pippo.ro/)

Sample code
---------------
First we must add in `src/main/resources/conf/application.properties`:

```
# List of hosts of the form "mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]"
mongodb.hosts = mongodb://localhost:27017
```

Add the following code sniped in your application:

```java
public class MyApplication extends Application {

    private MongoClient client;

    @Override
    protected void onInit() {
        this.client = MongoDBFactory.create(getPippoSettings());
        // add routes here
    }

    @Override
    protected RequestResponseFactory createRequestResponseFactory() {
        SessionDataStorage sessionDataStorage = new MongoDBSessionDataStorage(this.client.getDatabase("database"));
        SessionManager sessionManager = new SessionManager(sessionDataStorage);

        return new SessionRequestResponseFactory(this, sessionManager);
    }

    @Override
    protected void onDestroy() {
        this.client.close();
    }
}
```

TTL Index
---------------
The idle time of the session is manage with a TTL Index, read the [MongoDB Documentation](https://docs.mongodb.com/manual/core/index-ttl/) for more information
