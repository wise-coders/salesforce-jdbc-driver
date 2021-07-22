package com.dbschema.salesforce.schema;
/**
 * Copyright Wise Coders Gmbh. Redistribution allowed. Modifications only in https://bitbucket.org/dbschema/salesforce-jdbc-driver
 */
public class ForeignKey {

    public final Column column;
    public final Table targetTable;

    public ForeignKey( Column column, Table targetTable ){
        this.column = column;
        this.targetTable = targetTable;
    }
}
