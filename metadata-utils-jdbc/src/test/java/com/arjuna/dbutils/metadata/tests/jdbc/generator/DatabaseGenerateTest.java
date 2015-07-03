/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.jdbc.generator;

import java.net.URI;

import org.junit.Ignore;
import org.junit.Test;

import com.arjuna.dbutils.metadata.jdbc.generator.JDBCDatabaseMetadataGenerator;

import static org.junit.Assert.*;

public class DatabaseGenerateTest
{
    @Ignore
    @Test
    public void databaseView()
    {
        try
        {
            JDBCDatabaseMetadataGenerator jdbcDatabaseMetadataGenerator = new JDBCDatabaseMetadataGenerator();

            String rdf = jdbcDatabaseMetadataGenerator.generateDatabaseToRDF(URI.create("http://rdf.companieshouse.gov.uk/business_data"), "localhost", 5432, "databroker", "username", "password");

            assertNotNull("Unexpected non null RDF", rdf);
        }
        catch (Throwable throwable)
        {
            fail("Failed to generate Metadata Inventory");
        }
    }
}
