package com.wisecoders.dbschema.salesforce.schema;

/**
 * Copyright Wise Coders GmbH https://wisecoders.com
 * Driver is used in the DbSchema Database Designer https://dbschema.com
 * Free to be used by everyone.
 * Code modifications allowed only to GitHub repository https://github.com/wise-coders/salesforce-jdbc-driver
 */
public class ForeignKey {

    public final Column column;
    public final Table targetTable;

    public ForeignKey( Column column, Table targetTable ){
        this.column = column;
        this.targetTable = targetTable;
    }
}
