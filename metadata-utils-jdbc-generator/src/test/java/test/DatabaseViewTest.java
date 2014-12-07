/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package test;

import java.net.URI;

import org.junit.Test;

import com.arjuna.dbutils.metadata.jdbc.generator.JDBCDatabaseMetadataScan;

import static org.junit.Assert.*;

public class DatabaseViewTest
{
    @Test
    public void databaseView()
    {
        try
        {
           JDBCDatabaseMetadataScan jdbcDatabaseMetadataScan = new JDBCDatabaseMetadataScan();

           jdbcDatabaseMetadataScan.scanDataBase(URI.create("http://rdf.companieshouse.gov.uk/business_data"), "localhost", 5432, "databroker", "username", "password");
        }
        catch (Throwable throwable)
        {
            fail("Failed to populate Metadata Inventory");
        }
    }
}
