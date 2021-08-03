package com.dbschema.salesforce.schema;

import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.PartnerConnection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.dbschema.salesforce.SalesforceJdbcDriver.LOGGER;

/**
 * Copyright Wise Coders Gmbh. Redistribution allowed. Modifications only in https://bitbucket.org/dbschema/salesforce-jdbc-driver
 */
public class Schema {

    public final List<Table> tables = new ArrayList<>();
    private final ShowTables showTables;

    public Schema( ShowTables showTables ){
        this.showTables = showTables;
    }

    public void refreshTables( PartnerConnection connection ) throws SQLException {
        LOGGER.info("Load schema tables...");
        try {
            final List<Table> _tables = new ArrayList<>();
            for (DescribeGlobalSObjectResult desc : connection.describeGlobal().getSobjects()) {
                if ( desc.isQueryable() &&
                        ( showTables == ShowTables.all ||
                                ( showTables == ShowTables.custom && desc.isCustom() ) ||
                                ( showTables == ShowTables.intern && !desc.isCustom() ) ) ) {
                    _tables.add(new Table(desc.getName(), desc.isQueryable(), desc.getLabel() ));
                }
            }
            tables.clear();
            tables.addAll( _tables );
        } catch (Throwable ex) {
            throw new SQLException(ex);
        }
    }


    public void refreshColumns( PartnerConnection connection ) throws SQLException {
        LOGGER.info("Load schema columns..." );
        try {
            for ( Table table : tables ) {
                DescribeSObjectResult result = connection.describeSObject(table.getName());
                for (Field field : result.getFields()) {
                    Column column = table.createColumn( field.getName(), getType(field),
                            field.getLength(), field.getDigits(), field.getScale(),  field.isNillable(), field.isAutoNumber(), field.getLabel());
                    column.setCalculated(field.isCalculated() || field.isAutoNumber());
                    String[] referenceTos = field.getReferenceTo();
                    if (referenceTos != null) {
                        for (String referenceTo : referenceTos) {
                            Table pkTable = getTable( referenceTo );
                            if (pkTable != null) {
                                table.createForeignKey( column, pkTable );
                            }
                        }
                    }
                }
            }
        } catch ( Throwable ex ){
            throw new SQLException( ex );
        }
    }

    public void ensureTablesAreLoaded(PartnerConnection partnerConnection ) throws SQLException {
        if ( tables.isEmpty() ) {
            refreshTables( partnerConnection );
        }
    }

    public void ensureColumnsAreLoaded(PartnerConnection partnerConnection ) throws SQLException {
        ensureTablesAreLoaded( partnerConnection );
        for ( Table table : tables ) {
            if ( !table.columns.isEmpty() ) return;
        }
        refreshColumns( partnerConnection );
    }

    private static String getType(Field field) {
        String s = field.getType().toString();
        if (s.startsWith("_")) {
            s = s.substring("_".length());
        }
        return s.equalsIgnoreCase("double") ? "decimal" : s;
    }

    private Table getTable( String name ){
        for ( Table table : tables ){
            if ( name.equals( table.getName() )){
                return table;
            }
        }
        return null;
    }
}
