package com.dbschema.salesforce.io;

import com.dbschema.salesforce.SalesforceConnection;
import com.dbschema.salesforce.schema.Schema;
import com.dbschema.salesforce.schema.Table;
import com.sforce.soap.partner.PartnerConnection;
import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Copyright Wise Coders GmbH https://wisecoders.com
 * Driver is used in the DbSchema Database Designer https://dbschema.com
 * Free to be used by everyone.
 * Code modifications allowed only to GitHub repository https://github.com/wise-coders/salesforce-jdbc-driver
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