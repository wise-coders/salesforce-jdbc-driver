# SALESFORCE-JDBC-DRIVER
SALESFORCE-JDBC-DRIVER provided by [DbSchema Database Designer](https://www.dbschema.com).

The driver can be tested by downloading and installing DbSchema. DbSchema can be evaluated 15 days for free.
There is no need to register or download the driver - DbSchema will do everything for you.

The driver support all native SQL queries. For now only a read-only implementation is available. We will add write capabilities 
depending on the number of requests we get.

The driver is using internally an H2 database. 
We check each query which is executed and we transfer the table data into an internal H2 table.
Then we execute the queries in the H2 database. 
This allows us to use all possible joins ( LEFT, OUTER ), group by, order by, selects with wildcard ( SELECT * ), etc.

We implement also two custom SQL commands: 'reload schema' will read the schema metadata again, 'clean caches' and 'cache all' are related to cached data.
Cache all will take longer but then the queries will run faster.

```
Statement st = connection.createStatement();
st.execute("reload schema");
```

We appreciate any contributions to this repository. For questions you can contact [DbSchema Technical Support](https://www.dbschema.com/support.php)

# License

BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/saleforce-jdbc-driver.

# JDBC URL

jdbc:dbschema:salesforce://

or 

jdbc:dbschema:salesforce://sessionid=<sessionid>

Add tables=all to the JDBC URL to list all tables. By default are listed only custom tables.

```
Connection con = DriverManager.getConnection( "jdbc:dbschema:saleforce://username=lulu@yahoo.com;password=somepasswordwithtoken" );
Statement st = con.createStatement();
ResultSet rs = st.executeQuery("select * from UserRole")
while( rs.next() ){
    ....
}
```


# News 

Version 1.0 is released.