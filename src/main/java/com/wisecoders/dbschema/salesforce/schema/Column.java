package com.wisecoders.dbschema.salesforce.schema;

import java.io.Serializable;
import java.sql.Types;

/**
 * Licensed under <a href="https://creativecommons.org/licenses/by-nd/4.0/">CC BY-ND 4.0 DEED</a>, copyright <a href="https://wisecoders.com">Wise Coders GmbH</a>, used by <a href="https://dbschema.com">DbSchema Database Designer</a>.
 * Code modifications allowed only as pull requests to the <a href="https://github.com/wise-coders/salesforce-jdbc-driver">public GIT repository</a>.
 */
public class Column implements Serializable {

    public final Table table;
    public final String name, comment;
    private String type;
    public boolean calculated, nullable, autoIncrement;
    public int length, digits, scale;


    public Column(Table table, String name, String type, int length, int digits, int scale, boolean nullable, boolean autoIncrement, String comment ) {
        this.table = table;
        this.name = name;
        this.comment = comment;
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
