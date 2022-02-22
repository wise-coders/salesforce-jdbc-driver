package com.wisecoders.dbschema.salesforce.schema;

import java.sql.Types;

/**
 * Copyright Wise Coders GmbH https://wisecoders.com
 * Driver is used in the DbSchema Database Designer https://dbschema.com
 * Free to be used by everyone.
 * Code modifications allowed only to GitHub repository https://github.com/wise-coders/salesforce-jdbc-driver
 */

public class TypeInfo {

    public TypeInfo(String typeName, int javaSqlType, int precision, int minScale, int maxScale, int radix) {
        this.typeName = typeName;
        this.javaSqlType = javaSqlType;
        this.precision = precision;
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.radix = radix;
    }

    public String typeName;
    public int javaSqlType;
    public int precision;
    public int minScale;
    public int maxScale;
    public int radix;


    public static TypeInfo OTHER_TYPE_INFO = new TypeInfo("other", Types.OTHER, 0x7fffffff, 0, 0, 0);

    public static TypeInfo SALESFORCE_TYPES[] = {
            new TypeInfo("id", Types.VARCHAR, 0x7fffffff, 0, 0, 0),
            new TypeInfo("masterrecord", Types.VARCHAR, 0x7fffffff, 0, 0, 0),
            new TypeInfo("reference", Types.VARCHAR, 0x7fffffff, 0, 0, 0),
            new TypeInfo("string", Types.VARCHAR, 0x7fffffff, 0, 0, 0),
            new TypeInfo("encryptedstring", Types.VARCHAR, 0x7fffffff, 0, 0, 0),
            new TypeInfo("email", Types.VARCHAR, 0x7fffffff, 0, 0, 0),
            new TypeInfo("phone", Types.VARCHAR, 0x7fffffff, 0, 0, 0),
            new TypeInfo("url", Types.VARCHAR, 0x7fffffff, 0, 0, 0),
            new TypeInfo("textarea", Types.LONGVARCHAR, 0x7fffffff, 0, 0, 0),
            new TypeInfo("base64", Types.BLOB, 0x7fffffff, 0, 0, 0),
            new TypeInfo("boolean", Types.BOOLEAN, 1, 0, 0, 0),
            new TypeInfo("_boolean", Types.BOOLEAN, 1, 0, 0, 0),
            new TypeInfo("byte", Types.VARBINARY, 10, 0, 0, 10),
            new TypeInfo("_byte", Types.VARBINARY, 10, 0, 0, 10),
            new TypeInfo("int", Types.INTEGER, 10, 0, 0, 10),
            new TypeInfo("_int", Types.INTEGER, 10, 0, 0, 10),
            new TypeInfo("decimal", Types.DECIMAL, 17, -324, 306, 10),
            new TypeInfo("double", Types.DOUBLE, 17, -324, 306, 10),
            new TypeInfo("_double", Types.DOUBLE, 17, -324, 306, 10),
            new TypeInfo("percent", Types.DOUBLE, 17, -324, 306, 10),
            new TypeInfo("currency", Types.DOUBLE, 17, -324, 306, 10),
            new TypeInfo("date", Types.DATE, 10, 0, 0, 0),
            new TypeInfo("time", Types.TIME, 10, 0, 0, 0),
            new TypeInfo("datetime", Types.TIMESTAMP, 10, 0, 0, 0),
            new TypeInfo("picklist", Types.ARRAY, 0, 0, 0, 0),
            new TypeInfo("multipicklist", Types.ARRAY, 0, 0, 0, 0),
            new TypeInfo("combobox", Types.ARRAY, 0, 0, 0, 0),
            new TypeInfo("anyType", Types.OTHER, 0, 0, 0, 0),
    };
}
