package com.dbschema.salesforce.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Copyright Wise Coders GmbH https://wisecoders.com
 * Driver is used in the DbSchema Database Designer https://dbschema.com
 * Free to be used by everyone.
 * Code modifications allowed only to GitHub repository https://github.com/wise-coders/salesforce-jdbc-driver
 */
public class Table implements Serializable {

    public final String name, comment;
    public final Pattern findNamePattern;
    public final List<Column> columns = new ArrayList<>();
    public final List<ForeignKey> foreignKeys = new ArrayList<>();
    public final boolean isQueriable;
    private boolean isLoaded = false;

    public Table(String name, boolean isQueriable, String comment ) {
        this.name = name;
        this.isQueriable = isQueriable;
        this.comment = comment;
        // NAME SURROUNDED BY NON-LETTER-OR-DIGIT OR EOL
        this.findNamePattern = Pattern.compile("\\W" + name + "\\W|\\W" + name + "$", Pattern.DOTALL |Pattern.CASE_INSENSITIVE );
    }

    public Column createColumn ( String name, String type, int length, int digits, int scale, boolean nullable, boolean autoIncrement, String comment ){
        Column column = new Column( this, name, type, length, digits, scale, nullable, autoIncrement, comment );
        columns.add( column );
        return column;
    }

    public String getName() {
        return name;
    }

    public ForeignKey createForeignKey(Column fromColumn, Table targetTable ){
        ForeignKey fk = new ForeignKey( fromColumn, targetTable );
        foreignKeys.add( fk );
        return fk;
    }

    public String getColumnList(){
        StringBuilder sb = new StringBuilder();
        for ( Column column : columns ){
            if ( sb.length() > 0 ) sb.append(", ");
            sb.append( column );
        }
        return sb.toString();
    }

    public Column getColumn( String name ){
        for ( Column column : columns ){
            if ( name.equals( column.name )) return column;
        }
        return null;
    }

    public boolean isLoaded(){
        return isLoaded;
    }

    public void setLoaded( boolean loaded) {
        this.isLoaded = loaded;
    }

    @Override
    public String toString() {
        return name;
    }
}
