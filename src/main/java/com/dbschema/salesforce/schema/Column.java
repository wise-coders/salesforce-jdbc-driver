package com.dbschema.salesforce.schema;

import java.io.Serializable;
import java.sql.Types;

/**
 * Copyright Wise Coders Gmbh. Licensed under BSD License-3: free to use,distribution forbidden. Improvements accepted only in https://bitbucket.org/dbschema/salesforce-jdbc-driver
 */
public class Column implements Serializable {

    public final Table table;
    public final String name;
    private String type;
    public boolean calculated, nullable, autoIncrement;
    public int length, digits, scale;


    public Column(Table table, String name, String type, int length, int digits, int scale, boolean nullable, boolean autoIncrement ) {
        this.table = table;
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.length = length;
        this.digits = digits;
        this.scale = scale;
        this.autoIncrement = autoIncrement;
    }

    public String getType() {
        return type;
    }

    public int getJavaType(){
        for ( TypeInfo info : TypeInfo.SALESFORCE_TYPES ){
            if ( info.typeName.equalsIgnoreCase( type )){
                return info.javaSqlType;
            }
        }
        return Types.OTHER;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    public String getH2Type(){
        return "varchar";
    }


    @Override
    public String toString() {
        return name;
    }
}
