package com.wisecoders.dbschema.salesforce.io;

import com.wisecoders.dbschema.salesforce.SalesforceConnection;
import com.wisecoders.dbschema.salesforce.schema.Table;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;
import org.apache.commons.collections4.IteratorUtils;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.wisecoders.dbschema.salesforce.JdbcDriver.LOGGER;

/**
 * Copyright Wise Coders GmbH https://wisecoders.com
 * Driver is used in the DbSchema Database Designer https://dbschema.com
 * Free to be used by everyone.
 * Code modifications allowed only to GitHub repository https://github.com/wise-coders/salesforce-jdbc-driver
 */
public class TransferReader {

    private final SalesforceConnection salesforceConnection;

    public TransferReader(SalesforceConnection salesforceConnection) {
        this.salesforceConnection = salesforceConnection;
    }

    public void transferSchema() throws SQLException {
        LOGGER.info("Transfer schema..."  );
        salesforceConnection.ensureColumnsAreLoaded();
        for ( Table table : salesforceConnection.getSchemaDef().tables ) {
            TransferWriter writer = new TransferWriter(table, salesforceConnection);
            writer.createTable();
        }
    }

    public void transferAllData() throws SQLException {
        LOGGER.info("Transfer all data..."  );
        salesforceConnection.ensureColumnsAreLoaded();
        for ( Table table : salesforceConnection.getSchemaDef().tables ) {
            transferData( table );
        }
    }

    public void transferData( Table table ) throws SQLException {
        TransferWriter writer = new TransferWriter( table, salesforceConnection);
        salesforceConnection.ensureColumnsAreLoaded();
        LOGGER.info("Transfer '"  + table + "' data..." );
        Thread.dumpStack();
        writer.createTable();
        if ( table.isQueriable && !table.columns.isEmpty() ) {
            try {
                final Instant start = Instant.now();
                final String sql = "SELECT " + table.getColumnList() + " FROM " + table;
                QueryResult queryResult = null;
                int rows = 0;
                do {
                    queryResult = queryResult == null ? salesforceConnection.partnerConnection.query(sql) : salesforceConnection.partnerConnection.queryMore(queryResult.getQueryLocator());
                    SObject[] records = queryResult.getRecords();
                    for (SObject row : records) {
                        List<ForceResultField> clearRow = removeServiceInfo( row );
                        for ( ForceResultField field : clearRow ){
                            if ( field.getValue() instanceof List || field.getValue() instanceof Map ){
                                System.out.println("Here");
                            }
                        }
                        writer.transferRecord( clearRow );
                    }

                    rows+= records.length;
                } while (!queryResult.isDone());
                LOGGER.info("Transferred '" + table + "' " + rows + " rows in " + Duration.between( start, Instant.now()).getSeconds() + " sec" );
            } catch (Throwable ex) {
                LOGGER.log(Level.SEVERE, "Error transferring data", ex );
                ex.printStackTrace();
                //throw new SQLException(ex);
            }
        }
    }



    private List<List> removeServiceInfo(Iterator<XmlObject> rows) {
        return removeServiceInfo(IteratorUtils.toList(rows));
    }
    private List<List> removeServiceInfo(List<XmlObject> rows) {
        return rows.stream()
                .filter(this::isDataObjectType)
                .map(this::removeServiceInfo)
                .collect(Collectors.toList());
    }

    private List<ForceResultField> removeServiceInfo(XmlObject row) {
        List obj = IteratorUtils.toList(row.getChildren()).stream()
                .filter(this::isDataObjectType)
                .skip(1) // Removes duplicate Id from SF Partner API response
                // (https://developer.salesforce.com/forums/?id=906F00000008kciIAA)
                .map(field -> isNestedResultset(field)
                        ? removeServiceInfo(field.getChildren())
                        : toForceResultField(field))
                .collect(Collectors.toList());
        return (List<ForceResultField>)obj;
    }

    private ForceResultField toForceResultField(XmlObject field) {
        String fieldType = field.getXmlType() != null ? field.getXmlType().getLocalPart() : null;
        if ("sObject".equalsIgnoreCase(fieldType)) {
            List<XmlObject> children = new ArrayList<>();
            field.getChildren().forEachRemaining(children::add);
            field = children.get(2);
        }
        String name = field.getName().getLocalPart();
        Object value = field.getValue();
        return new ForceResultField(null, fieldType, name, value);
    }

    private boolean isNestedResultset(XmlObject object) {
        return object.getXmlType() != null && "QueryResult".equals(object.getXmlType().getLocalPart());
    }

    private final static List<String> SOAP_RESPONSE_SERVICE_OBJECT_TYPES = Arrays.asList("type", "done", "queryLocator", "size");

    private boolean isDataObjectType(XmlObject object) {
        return !SOAP_RESPONSE_SERVICE_OBJECT_TYPES.contains(object.getName().getLocalPart());
    }


}
