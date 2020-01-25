/*
 * Copyright 2009-2010 Data Archiving and Networked Services (DANS), Netherlands.
 *
 * This file is part of DANS DBF Library.
 *
 * DANS DBF Library is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * DANS DBF Library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with DANS DBF Library. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package com.dbschema.salesforce;

import org.junit.Before;
import org.junit.Test;

import java.sql.*;


public class TestConnectivity {

    private static Connection connection;



    @Before
    public void testDriver() throws SQLException {
        new SalesforceJdbcDriver();
        final String URL = "jdbc:dbschema:salesforce://user=dragospruteanu@yahoo.com&password=dragos125Opv1Gbkbb5K7ayRNegM7EkMp";
        connection = DriverManager.getConnection( URL );
    }

    @Test
    public void test() throws SQLException {
        Statement st = connection.createStatement();
        if( st.execute("select * from UserRole")){
            ResultSet rs = st.getResultSet();
            while( rs.next() ){
                for( int i = 0; i < rs.getMetaData().getColumnCount(); i++ ){
                    System.out.print(rs.getString(i+1)+ ",");
                }
                System.out.println();
            }
        }
        //st.execute("save dbf to out/testExport");
    }

}
