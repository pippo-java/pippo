Pippo Session JDBC
=====================
[JDBC](https://docs.oracle.com/javase/tutorial/jdbc/) Session Manager integration for [Pippo](http://www.pippo.ro/)

Sample code
---------------

Add the following code sniped in your application:

```java
public class MyApplication extends Application {

    private DataSource dataSource;

    @Override
    protected void onInit() {
        this.dataSource = createDataSource();
        // add routes here
    }

    @Override
    protected RequestResponseFactory createRequestResponseFactory() {
        SessionDataStorage sessionDataStorage = new JDBCSessionDataStorage(dataSource);
        SessionManager sessionManager = new SessionManager(sessionDataStorage);

        return new SessionRequestResponseFactory(this, sessionManager);
    }
}
```

How to create a DataSource
---------------

If you are using an application server, you can use JNDI

```java
Context initialContext = new InitialContext();
DataSource datasource = (DataSource)initialContext.lookup("jdbc/test");
```

If it is a standalone application, you can use [C3P0](http://www.mchange.com/projects/c3p0/index.html) or [DBCP2](http://commons.apache.org/proper/commons-dbcp/)

**C3P0**
```java
ComboPooledDataSource cpds = new ComboPooledDataSource();
try {
cpds.setDriverClass("org.h2.Driver");
} catch (PropertyVetoException ex) {
}
cpds.setJdbcUrl("jdbc:h2:mem:test");
cpds.setUser("sa");
cpds.setPassword("sa");
DataSource dataSource = cpds;
```

**DBCP2**
```java
BasicDataSource bds = new BasicDataSource();
bds.setDriverClassName("org.h2.Driver");
bds.setUrl("jdbc:h2:mem:test");
bds.setUsername("sa");
bds.setPassword("sa");
DataSource dataSource = bds;
```

Warning
---------------

Pippo Session JDBC does not have a mechanism for deleting expired sessions, it is recommended that the deletion of expired sessions will by realize from the database based on the mechanism of each provider.

MySQL Example
---------------

```sql
CREATE TABLE `pippo`.`session` (
  `id` VARCHAR(32) NOT NULL,
  `time` DATETIME NOT NULL,
  `data` BLOB NOT NULL,
  PRIMARY KEY (`id`));

SET GLOBAL event_scheduler = ON;

DELIMITER $$

CREATE EVENT `delete_expire_session` 
ON SCHEDULE EVERY 30 MINUTE
DO BEGIN
DELETE FROM `pippo`.`session` WHERE TIMESTAMPDIFF(MINUTE, time, CURRENT_TIMESTAMP()) > 30;
END $$

DELIMITER ;
```
