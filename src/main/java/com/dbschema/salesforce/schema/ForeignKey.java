package com.dbschema.salesforce.schema;
/**
 * Copyright Wise Coders Gmbh. Licensed under BSD License-3: free to use,distribution forbidden. Improvements accepted only in https://bitbucket.org/dbschema/salesforce-jdbc-driver
 */
public class ForeignKey {

    public final Column column;
    public final Table targetTable;

    public ForeignKey( Column column, Table targetTable ){
        this.column = column;
        this.targetTable = targetTable;
    }
}
