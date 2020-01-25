package com.dbschema.salesforce;

import com.dbschema.salesforce.io.ArrayResultSet;
import com.dbschema.salesforce.schema.Column;
import com.dbschema.salesforce.schema.ForeignKey;
import com.dbschema.salesforce.schema.Table;

import java.sql.*;

/**
 * Copyright Wise Coders Gmbh. Licensed under BSD License-3: free to use,distribution forbidden. Improvements accepted only in https://bitbucket.org/dbschema/salesforce-jdbc-driver
 */
public class SalesforceMetaData implements DatabaseMetaData {

    private static final String DEFAULT_SCHEMA_NAME = "Default";

    private final SalesforceConnection connection;

    public SalesforceMetaData( SalesforceConnection connection ){
        this.connection = connection;
    }
    
    private DatabaseMetaData getH2Meta() throws SQLException{
        return connection.h2Connection.getMetaData();
    }


    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        connection.ensureTablesAreLoaded();
        final ArrayResultSet resultSet = new ArrayResultSet();
        resultSet.setColumnNames(new String[]{"TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
                "TABLE_TYPE", "REMARKS", "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "SELF_REFERENCING_COL_NAME",
                "REF_GENERATION"});
        for (Table table: connection.getSchemaDef().tables ) {
            resultSet.addRow(new String[]{null, DEFAULT_SCHEMA_NAME, table.name, "TABLE", "", "", "", "", "", ""});
        }
        return resultSet;
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        final ArrayResultSet resultSet = new ArrayResultSet();
        resultSet.setColumnNames(new String[]{"TABLE_SCHEM", "TABLE_CATALOG"});
        resultSet.addRow( new String[]{DEFAULT_SCHEMA_NAME, null});
        return resultSet;
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        final ArrayResultSet resultSet = new ArrayResultSet();
        resultSet.setColumnNames(new String[]{"TABLE_CAALOG"});
        return resultSet;
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        connection.ensureColumnsAreLoaded();
        final ArrayResultSet result = new ArrayResultSet();
        result.setColumnNames(new String[] { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME",
                "DATA_TYPE", "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "NUM_PREC_RADIX",
                "NULLABLE", "REMARKS", "COLUMN_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH",
                "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATLOG", "SCOPE_SCHEMA", "SCOPE_TABLE",
                "SOURCE_DATA_TYPE", "IS_AUTOINCREMENT" });
        for ( Table table : connection.getSchemaDef().tables ){
            if ( tableNamePattern == null || table.name.contains(tableNamePattern)){
                for ( Column column : table.columns ){
                    if ( columnNamePattern == null|| column.name.contains( columnNamePattern )) {
                        result.addRow( new String[]{
                                null, // "TABLE_CAT",
                                DEFAULT_SCHEMA_NAME, // "TABLE_SCHEMA",
                                table.name, // "TABLE_NAME", (i.e. MongoDB Collection Name)
                                column.name, // "COLUMN_NAME",
                                "" + column.getJavaType(), // "DATA_TYPE",
                                column.getType(), // "TYPE_NAME",
                                "" + Math.max( column.length, column.digits), // "COLUMN_SIZE",
                                "0", // "BUFFER_LENGTH", (not used)
                                "" + column.scale, // "DECIMAL_DIGITS",
                                "10", // "NUM_PREC_RADIX",
                                "" + ( column.nullable ? columnNullable : columnNoNulls ), // "NULLABLE",
                                "", // "REMARKS",
                                "", // "COLUMN_DEF",
                                "0", // "SQL_DATA_TYPE", (not used)
                                "0", // "SQL_DATETIME_SUB", (not used)
                                "800", // "CHAR_OCTET_LENGTH",
                                "1", // "ORDINAL_POSITION",
                                "NO", // "IS_NULLABLE",
                                null, // "SCOPE_CATLOG", (not a REF type)
                                null, // "SCOPE_SCHEMA", (not a REF type)
                                null, // "SCOPE_TABLE", (not a REF type)
                                null, // "SOURCE_DATA_TYPE", (not a DISTINCT or REF type)
                                column.autoIncrement ? "YES" : "NO" // "IS_AUTOINCREMENT" (can be auto-generated, but can also be specified)
                        });
                    }
                }
            }
        }
        return result;
    }


    @Override
    public ResultSet getPrimaryKeys(String catalog, String schemaName, String tableName) throws SQLException {
        connection.ensureColumnsAreLoaded();
        ArrayResultSet result = new ArrayResultSet();
        result.setColumnNames(new String[] { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME",
                "KEY_SEQ", "PK_NAME" });
        for ( Table table : connection.getSchemaDef().tables){
            if ( table.name.equals( tableName )){
                for ( Column column : table.columns ){
                    if ( "Id".equals(column.name )) {
                        result.addRow(new String[]{
                                null, DEFAULT_SCHEMA_NAME, table.name, column.name, "1", "Pk_" + tableName
                        });
                    }
                }
            }
        }
        return result;
    }


    @Override
    public ResultSet getImportedKeys(String catalogName, String schemaName, String tableNamePattern ) throws SQLException {
        connection.ensureColumnsAreLoaded();
        final ArrayResultSet result = new ArrayResultSet();
        result.setColumnNames(new String[]{"PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME", "FKTABLE_CAT", "FKTABLE_SCHEM",
                "FKTABLE_NAME", "FKCOLUMN_NAME", "KEY_SEQ", "UPDATE_RULE", "DELETE_RULE", "FK_NAME", "PK_NAME", "DEFERRABILITY"});

        for ( Table table : connection.getSchemaDef().tables ){
            if ( tableNamePattern == null || table.name.contains( tableNamePattern )){
                for ( ForeignKey reference : table.foreignKeys ){
                    result.addRow(new String[] {
                            null, //PKTABLE_CAT
                            DEFAULT_SCHEMA_NAME, //PKTABLE_SCHEMA
                            reference.targetTable.name,//PKTABLE_NAME
                            "Id", //PKCOLUMN_NAME
                            null,//FKTABLE_CAT
                            DEFAULT_SCHEMA_NAME, //FKTABLE_SCHEM
                            table.name, //FKTABLE_NAME
                            reference.column.name,//FKCOLUMN_NAME
                            "1",//KEY_SEQ 1,2
                            ""+ DatabaseMetaData.importedKeyNoAction, //UPDATE_RULE
                            ""+DatabaseMetaData.importedKeyNoAction, //DELETE_RULE
                            "Ref", //FK_NAME
                            null, //PK_NAME
                            ""+DatabaseMetaData.importedKeyInitiallyImmediate //DEFERRABILITY
                    });
                }
            }
        }
        return result;
    }


    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        return null;
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return getH2Meta().allProceduresAreCallable();
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return getH2Meta().allTablesAreSelectable();
    }

    @Override
    public String getURL() throws SQLException {
        return getH2Meta().getURL();
    }

    @Override
    public String getUserName() throws SQLException {
        return getH2Meta().getUserName();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return getH2Meta().isReadOnly();
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return getH2Meta().nullsAreSortedHigh();
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return getH2Meta().nullsAreSortedLow();
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return getH2Meta().nullsAreSortedAtStart();
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return getH2Meta().nullsAreSortedAtEnd();
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return getH2Meta().getDatabaseProductName();
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return getH2Meta().getDatabaseProductVersion();
    }

    @Override
    public String getDriverName() throws SQLException {
        return getH2Meta().getDriverName();
    }

    @Override
    public String getDriverVersion() throws SQLException {
        return getH2Meta().getDriverVersion();
    }

    @Override
    public int getDriverMajorVersion() {
        return 11;
    }

    @Override
    public int getDriverMinorVersion() {
        return 1;
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        return getH2Meta().usesLocalFiles();
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return getH2Meta().usesLocalFilePerTable();
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return getH2Meta().supportsMixedCaseIdentifiers();
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return getH2Meta().storesUpperCaseIdentifiers();
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return getH2Meta().storesLowerCaseIdentifiers();
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return getH2Meta().storesMixedCaseIdentifiers();
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return getH2Meta().supportsMixedCaseIdentifiers();
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return getH2Meta().storesUpperCaseQuotedIdentifiers();
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return getH2Meta().storesLowerCaseQuotedIdentifiers();
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return storesMixedCaseQuotedIdentifiers();
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        return getH2Meta().getIdentifierQuoteString();
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        return getH2Meta().getSQLKeywords();
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        return getH2Meta().getNumericFunctions();
    }

    @Override
    public String getStringFunctions() throws SQLException {
        return getH2Meta().getStringFunctions();
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        return getH2Meta().getSystemFunctions();
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        return getH2Meta().getTimeDateFunctions();
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        return getH2Meta().getSearchStringEscape();
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        return getH2Meta().getExtraNameCharacters();
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return false;
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return false;
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        return null;
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        return null;
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        return null;
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return false;
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        return null;
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return false;
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxConnections() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        return 0;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return false;
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxStatements() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return 0;
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        return getH2Meta().getProcedures( catalog, schemaPattern, procedureNamePattern);
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        return getProcedureColumns( catalog, schemaPattern, procedureNamePattern, columnNamePattern);
    }


    @Override
    public ResultSet getTableTypes() throws SQLException {
        return null;
    }


    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        return null;
    }


    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        return getH2Meta().getTypeInfo();
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        return getH2Meta().getIndexInfo( catalog, schema, table, unique, approximate);
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        return false;
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        return null;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getH2Meta().getConnection();
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
        return null;
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getSQLStateType() throws SQLException {
        return 0;
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
       return getSchemas();
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return false;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        return getH2Meta().getFunctions( catalog, schemaPattern, functionNamePattern );
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return null;
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
