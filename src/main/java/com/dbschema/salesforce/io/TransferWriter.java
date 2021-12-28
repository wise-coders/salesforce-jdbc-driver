package com.dbschema.salesforce.io;

import com.dbschema.salesforce.SalesforceConnection;
import com.dbschema.salesforce.schema.Column;
import com.dbschema.salesforce.schema.Table;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.logging.Level;

import static com.dbschema.salesforce.SalesforceJdbcDriver.LOGGER;

/**
 * Copyright Wise Coders Gmbh. Redistribution allowed. Modifications only in https://bitbucket.org/dbschema/salesforce-jdbc-driver
 */
public class TransferWriter {

    public final static char QUOTE_CHAR = '"';

    private String insertSql;
    private final SalesforceConnection salesforceConnection;
    private final Table table;


    public TransferWriter(Table table, SalesforceConnection salesforceConnection) {
        this.salesforceConnection = salesforceConnection;
        this.table = table;
    }

    public void createTable() throws SQLException {
        LOGGER.log(Level.INFO, "Transfer table '" + table.name + "'");
        final StringBuilder createSb = new StringBuilder("create table ").append(QUOTE_CHAR).append(table.name).append(QUOTE_CHAR).append("(\n");
        final StringBuilder insertSb = new StringBuilder("insert into ").append(QUOTE_CHAR).append(table.name).append(QUOTE_CHAR).append("(");
        final StringBuilder insertValuesSb = new StringBuilder("values(");
        boolean appendComma = false;
        for ( Column column : table.columns ) {

            if (appendComma) {
                createSb.append(",\n");
                insertSb.append(",");
                insertValuesSb.append(",");
            }
            createSb.append("\t").append(QUOTE_CHAR).append(column).append(QUOTE_CHAR).append(" ");
            insertSb.append(QUOTE_CHAR).append(column).append(QUOTE_CHAR);
            insertValuesSb.append("?");
            createSb.append( column.getH2Type());
            appendComma = true;
        }
        createSb.append(")");
        insertSb.append(")");
        insertValuesSb.append(")");

        final String dropTableSQL = "drop table if exists " + QUOTE_CHAR + table.name + QUOTE_CHAR;
        //LOGGER.log(Level.INFO, dropTableSQL);
        salesforceConnection.h2Connection.prepareStatement(dropTableSQL).execute();
        salesforceConnection.h2Connection.commit();


        //LOGGER.log(Level.INFO, createSb.toString());
        salesforceConnection.h2Connection.prepareStatement(createSb.toString()).execute();
        salesforceConnection.h2Connection.commit();

        /*
        THIS CAN BE USED TO WRITE DATA BACK TO SALESFORCE
        String createTriggerSQL = "CREATE TRIGGER " + QUOTE_CHAR + "trg_" + table.name + QUOTE_CHAR +
                "BEFORE UPDATE, INSERT, DELETE ON " + QUOTE_CHAR + table.name + QUOTE_CHAR +
                " FOR EACH ROW\n" +
                " CALL \"com.dbschema.salesforce.io.H2Trigger\"";

        salesforceConnection.h2Connection.prepareStatement( createTriggerSQL ).execute();
        salesforceConnection.h2Connection.commit();
        */

        this.insertSql = insertSb.toString() + insertValuesSb.toString();
    }


    public void transferRecord(List<ForceResultField> fields ) throws Exception {
        final PreparedStatement stInsert = salesforceConnection.h2Connection.prepareStatement(insertSql);
        for ( ForceResultField field: fields ){
            Object value = field.getValue();
            Column column = table.getColumn( field.getName() );
            int i = table.columns.indexOf( column );
            if (value == null) {
                stInsert.setNull(i+1, Types.VARCHAR );
            } else {
                stInsert.setObject(i+1, value);
            }
        }
        stInsert.execute();
        stInsert.close();
        salesforceConnection.h2Connection.commit();
    }





}
