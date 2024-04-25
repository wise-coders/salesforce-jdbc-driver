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
package com.wisecoders.dbschema.salesforce;

import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * Licensed under <a href="https://creativecommons.org/licenses/by-nd/4.0/">CC BY-ND 4.0 DEED</a>, copyright <a href="https://wisecoders.com">Wise Coders GmbH</a>, used by <a href="https://dbschema.com">DbSchema Database Designer</a>.
 * Code modifications allowed only as pull requests to the <a href="https://github.com/wise-coders/salesforce-jdbc-driver">public GIT repository</a>.
 */
public class TestConnectivity {

    private static Connection connection;

    @Before
    public void testDriver() throws SQLException, IOException {
        new JdbcDriver();
        FileInputStream input = new FileInputStream("gradle.properties");
        Properties prop = new Properties();
        prop.load(input);
        // IN gradle.properties EDIT salesforceURL=jdbc:dbschema:salesforce://user=...&password=...+token
        final String URL = prop.getProperty("salesforceURL");
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
