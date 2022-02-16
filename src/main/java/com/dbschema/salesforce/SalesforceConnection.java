package com.dbschema.salesforce;

import com.dbschema.salesforce.io.TransferReader;
import com.dbschema.salesforce.schema.Schema;
import com.dbschema.salesforce.schema.ShowTables;
import com.dbschema.salesforce.schema.Table;
import com.sforce.soap.partner.PartnerConnection;
import org.h2.jdbc.JdbcConnection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;


/**
 * When you open a connection we load internally the list of tables.
 * When you execute a query, we check which table names can be found in the query and we transfer them in an internal H2 database stored in user.home/.DbSchema
 * We also create a proxy on Statement and intercept certain commands we implement in the driver.
 * The driver can be improved, we are happy for contributions.
 *
 *
 * Copyright Wise Coders GmbH https://wisecoders.com
 * Driver is used in the DbSchema Database Designer https://dbschema.com
 * Free to be used by everyone.
 * Code modifications allowed only to GitHub repository https://github.com/wise-coders/salesforce-jdbc-driver
 */

public class SalesforceConnection implements Connection {

    private static final Pattern CLEAN_CACHES = Pattern.compile( "(\\s*)clean(\\s+)caches(\\s+)", Pattern.CASE_INSENSITIVE );
    private static final Pattern CACHE_ALL = Pattern.compile( "(\\s*)cache(\\s+)all(\\s+)", Pattern.CASE_INSENSITIVE );
    private static final Pattern RELOAD_SCHEMA = Pattern.compile( "(\\s*)reload(\\s+)schema(\\s+)", Pattern.CASE_INSENSITIVE );

    private final String dbId;
    public final JdbcConnection h2Connection;
    public final PartnerConnection partnerConnection;
    private final TransferReader reader;
    private static final HashMap<String,Schema> schemes = new HashMap<>();


    SalesforceConnection(String dbId, JdbcConnection h2Connection, PartnerConnection partnerConnection, Map<String,String> parameters ){
        this.dbId = dbId;
        this.h2Connection = h2Connection;
        this.partnerConnection = partnerConnection;
        ShowTables showTables = ShowTables.all;
        if ( parameters.containsKey("tables")){
            if( "all".equalsIgnoreCase(parameters.get("tables"))) showTables = ShowTables.all;
            else if( "custom".equalsIgnoreCase(parameters.get("tables"))) showTables = ShowTables.custom;
        }
        if ( !schemes.containsKey( dbId )) {
            schemes.put(dbId, new Schema( showTables ));
        }
        this.reader = new TransferReader( this );
    }

    public Schema getSchemaDef(){
        return schemes.get( dbId );
    }

    public static Schema getSchema( String schemaName ){
        return schemes.get( schemaName );
    }

    void ensureTablesAreLoaded() throws SQLException {
        getSchemaDef().ensureTablesAreLoaded( partnerConnection );
    }

    public void ensureColumnsAreLoaded() throws SQLException {
        getSchemaDef().ensureColumnsAreLoaded( partnerConnection );
    }

    private void transferDataForTablesFromQuery( String query ) throws SQLException{
        if ( query != null && !query.isEmpty()){
            ensureTablesAreLoaded();
            for ( Table table : getSchemaDef().tables ){
                if (!table.isLoaded() && table.findNamePattern.matcher(query).find() ){
                    reader.transferData( table );
                    table.setLoaded( true );
                }
            }
        }
    }

    @Override
    public Statement createStatement() throws SQLException {
        Statement statement = h2Connection.createStatement();
        return new StatementProxy(statement).proxyStatement;
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        Statement statement = h2Connection.createStatement(resultSetType, resultSetConcurrency );
        return new StatementProxy(statement).proxyStatement;
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        Statement statement = h2Connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability );
        return new StatementProxy(statement).proxyStatement;
    }


    private class StatementProxy implements InvocationHandler {

        private final Object target;
        final Statement proxyStatement;

        StatementProxy(Object target) {
            this.target = target;
            this.proxyStatement = (Statement)Proxy.newProxyInstance(Statement.class.getClassLoader(),
                    new Class[]{Statement.class},
                    this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ( args != null && args.length > 0 && args[0] != null ) {
                final String firstArgument = args[0].toString();
                if ( RELOAD_SCHEMA.matcher(firstArgument).matches() ){
                    getSchemaDef().refreshTables( partnerConnection );
                    getSchemaDef().refreshColumns( partnerConnection );
                } else if ( CACHE_ALL.matcher(firstArgument).matches()){
                    new TransferReader(SalesforceConnection.this).transferAllData();
                } if ( CLEAN_CACHES.matcher(firstArgument).matches()){
                    for ( Table table : getSchemaDef().tables ){
                        table.setLoaded( false );
                    }
                } else {
                    transferDataForTablesFromQuery(firstArgument);
                }
            }
            return method.invoke(target, args);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        transferDataForTablesFromQuery( sql );
        return h2Connection.prepareStatement( sql );
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        transferDataForTablesFromQuery( sql );
        return h2Connection.prepareCall( sql );
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        transferDataForTablesFromQuery( sql );
        return h2Connection.nativeSQL( sql );
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        h2Connection.setAutoCommit( autoCommit );
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return h2Connection.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        h2Connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        h2Connection.rollback();
    }

    @Override
    public void close() throws SQLException {
        h2Connection.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return h2Connection.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() {
        return new SalesforceMetaData( this );
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        h2Connection.setReadOnly( readOnly );
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return h2Connection.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        h2Connection.setCatalog( catalog );
    }

    @Override
    public String getCatalog() throws SQLException {
        return h2Connection.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        h2Connection.setTransactionIsolation( level );
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return h2Connection.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return h2Connection.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        h2Connection.clearWarnings();
    }



    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        transferDataForTablesFromQuery( sql );
        return h2Connection.prepareStatement( sql, resultSetType, resultSetConcurrency );
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        transferDataForTablesFromQuery( sql );
        return h2Connection.prepareCall( sql, resultSetType, resultSetConcurrency );
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return h2Connection.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        h2Connection.setTypeMap( map );
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        h2Connection.setHoldability( holdability );
    }

    @Override
    public int getHoldability() throws SQLException {
        return h2Connection.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return h2Connection.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return h2Connection.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        h2Connection.rollback(savepoint );
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        h2Connection.releaseSavepoint( savepoint );
    }



    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        transferDataForTablesFromQuery( sql );
        return h2Connection.prepareStatement( sql, resultSetType, resultSetConcurrency, resultSetHoldability );
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        transferDataForTablesFromQuery(sql );
        return h2Connection.prepareCall( sql, resultSetType, resultSetConcurrency, resultSetHoldability );
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        transferDataForTablesFromQuery( sql );
        return h2Connection.prepareStatement( sql, autoGeneratedKeys );
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        transferDataForTablesFromQuery( sql );
        return h2Connection.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        transferDataForTablesFromQuery( sql );
        return h2Connection.prepareStatement( sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return h2Connection.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return h2Connection.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return h2Connection.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return h2Connection.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return h2Connection.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        h2Connection.setClientInfo( name, value );
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        h2Connection.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return h2Connection.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return h2Connection.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return h2Connection.createArrayOf( typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return h2Connection.createStruct( typeName, attributes );
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        h2Connection.setSchema( schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return h2Connection.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        h2Connection.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        h2Connection.setNetworkTimeout( executor, milliseconds );
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return h2Connection.getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return h2Connection.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return h2Connection.isWrapperFor( iface );
    }
}
