/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.xssf.generator;

import java.io.File;
import java.net.URI;

import org.junit.Test;

import com.arjuna.dbutils.metadata.xssf.generator.XSSFSpeadsheetMetadataGenerator;

import static org.junit.Assert.*;

public class XSSFSpreadsheetGeneratorTest
{
    @Test
    public void generateXSSFSpeadsheetMetadata()
    {
        try
        {
            File spreadsheetFile = new File("Test01.xlsx");
            XSSFSpeadsheetMetadataGenerator xssfSpeadsheetMetadataGenerator = new XSSFSpeadsheetMetadataGenerator();

            String rdf = xssfSpeadsheetMetadataGenerator.generateXSSFSpeadsheetMetadata(URI.create("http://rdf.data.org/example_xssf"), spreadsheetFile);

            assertNotNull("Unexpected non null RDF", rdf);
        }
        catch (Throwable throwable)
        {
            fail("Failed to generate Metadata Inventory");
        }
    }
}
