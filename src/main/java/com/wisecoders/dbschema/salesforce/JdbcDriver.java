package com.wisecoders.dbschema.salesforce;


import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.wisecoders.dbschema.salesforce.io.H2Trigger;
import com.wisecoders.dbschema.salesforce.io.Util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.*;

/**
 * When you open a connection we load internally the list of tables.
 * When you execute a query, we check which table names can be found in the query and we transfer them in an internal H2 database stored in user.home/.DbSchema
 * We also create a proxy on Statement and intercept certain commands we implement in the driver.
 * The driver can be improved, we are happy for contributions.
 *
 */
public class JdbcDriver implements Driver {

    private static final String JDBC_PREFIX = "jdbc:dbschema:salesforce://";
    public static final Logger LOGGER = Logger.getLogger( JdbcDriver.class.getName() );


    static {
        try {
            DriverManager.registerDriver( new JdbcDriver());
            LOGGER.setLevel(Level.ALL);

            final ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(consoleHandler);

            final FileHandler fileHandler = new FileHandler(System.getProperty("user.home") + "/.DbSchema/logs/SalesforceJdbcDriver.log");
            fileHandler.setFormatter( new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            LOGGER.addHandler(fileHandler);
        } catch ( Exception ex ){
            ex.printStackTrace();
        }
    }


    @Override
    public Connection connect( String url, Properties info ) throws SQLException {
        if ( acceptsURL(JDBC_PREFIX)) {
            final Map<String,String> parameters = new HashMap<>();
            if ( info != null ){
                for ( Object key : info.keySet()){
                    parameters.put( key.toString(), info.get( key ).toString() );
                }
            }
            String data = url.substring(JDBC_PREFIX.length());
            String hostRef = data;
            int idxParam = data.indexOf("?");
            if ( idxParam > -1 ){
                hostRef = data.substring( 0, idxParam );
                String paramStr = data.substring( idxParam + 1 );
                for ( String pair: paramStr.split("&")){
                    String[] pairArray = pair.split("=");
                    if( pairArray.length == 2 ){
                        parameters.put( pairArray[0].toLowerCase(), pairArray[1]);
                    }
                }
            }
            final String userName = parameters.get("user");
            final String password = parameters.get("password");
            final String sessionId = parameters.get("sessionid");
            final ConnectorConfig config = new ConnectorConfig();
            if ( hostRef.length() == 0 ){
                hostRef = "login.salesforce.com/services/Soap/u/51.0";
            }
            config.setAuthEndpoint( String.format( hostRef ));
            LOGGER.info("Connect to endpoint '" + hostRef + "' using " + (sessionId != null ? "sessionid" : "user/password") );
            if (sessionId == null) {
                if (userName == null) throw new SQLException("Missing username. Please add it to URL as user=<value>");
                if (password == null) throw new SQLException("Missing password. Please add it to URL as password=<value>");
                java.net.Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                userName,
                                password.toCharArray());
                    }
                });
                config.setUsername(userName);
                config.setPassword(password);
            } else {
                config.setSessionId( sessionId );
            }
            try {
                final PartnerConnection partnerConnection = Connector.newConnection(config);
                final String h2DbName = Util.md5Java(userName != null ? userName : sessionId);
                H2Trigger.partnerConnection = partnerConnection;

                return new SalesforceConnection( h2DbName, partnerConnection, parameters);
            } catch ( ConnectionException ex ){
                throw new SQLException( ex.getLocalizedMessage(), ex );
            }
        } else {
            throw new SQLException("Incorrect URL. Expected jdbc:dbschema:salesforce://https://login|OTHER.salesforce.com/services/Soap/u/APIVERSION?<parameters>");
        }
    }


    // https://login.salesforce.com/services/Soap/u/51.0


    @Override
    public boolean acceptsURL(String url) {
        return url.startsWith(JDBC_PREFIX);
    }

    static class ExtendedDriverPropertyInfo extends DriverPropertyInfo {
        ExtendedDriverPropertyInfo( String name, String value, String[] choices, String description ){
            super( name, value);
            this.description = description;
            this.choices = choices;
        }
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        DriverPropertyInfo[] result = new DriverPropertyInfo[1];
        result[0] = new ExtendedDriverPropertyInfo("log", "true", new String[]{"true", "false"}, "Activate driver INFO logging");
        return result;
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() {
        return null;
    }







}
