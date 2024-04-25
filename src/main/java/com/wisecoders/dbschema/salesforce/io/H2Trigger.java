package com.wisecoders.dbschema.salesforce.io;

import com.sforce.soap.partner.PartnerConnection;
import com.wisecoders.dbschema.salesforce.SalesforceConnection;
import com.wisecoders.dbschema.salesforce.schema.Schema;
import com.wisecoders.dbschema.salesforce.schema.Table;
import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Licensed under <a href="https://creativecommons.org/licenses/by-nd/4.0/">CC BY-ND 4.0 DEED</a>, copyright <a href="https://wisecoders.com">Wise Coders GmbH</a>, used by <a href="https://dbschema.com">DbSchema Database Designer</a>.
 * Code modifications allowed only as pull requests to the <a href="https://github.com/wise-coders/salesforce-jdbc-driver">public GIT repository</a>.
 */

public class H2Trigger implements Trigger {

    public static PartnerConnection partnerConnection;

    private Table table;

    @Override
    public void init(Connection conn, String schemaName,
                     String triggerName, String tableName, boolean before, int type)
            throws SQLException {
        Schema schema = SalesforceConnection.getSchema( schemaName );
        if ( schema != null ){
            table = schema.getTable( tableName );
        }

    }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow)
            throws SQLException {
        if ( table != null && partnerConnection != null ) {
            if (oldRow != null && newRow != null) {
                StringBuilder sb = new StringBuilder("INSERT INTO \"").append(table.name).append("\"( ").append(table.getColumnList()).append("\" VALUES (");
                for (int i = 0; i < newRow.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append("?");
                }
                sb.append(")");
            }
        }
    }

    @Override
    public void close() throws SQLException {}

    @Override
    public void remove() throws SQLException {}
}