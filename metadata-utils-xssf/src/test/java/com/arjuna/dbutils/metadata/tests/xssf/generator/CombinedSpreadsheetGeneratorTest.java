/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.xssf.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.arjuna.dbutils.metadata.xssf.generator.StreamedXSSFSpreadsheetMetadataGenerator;
import com.arjuna.dbutils.metadata.xssf.generator.XSSFSpreadsheetMetadataGenerator;

import static org.junit.Assert.*;

public class CombinedSpreadsheetGeneratorTest
{
    private static final Logger logger = Logger.getLogger(StreamedXSSFSpreadsheetMetadataGenerator.class.getName());

    @Test
    public void compairXSSFSpreadsheetMetadataData()
    {
        try
        {
            File        spreadsheetFile        = new File("Test01.xlsx");
            byte[]      spreadsheetData        = new byte[(int) spreadsheetFile.length()];
            InputStream spreadsheetInputStream = new FileInputStream(spreadsheetFile);
            int         character              = spreadsheetInputStream.read();
            int         index                  = 0;
            while (character != -1)
            {
                spreadsheetData[index] = (byte) character;
                character = spreadsheetInputStream.read();
                index++;
            }
            spreadsheetInputStream.close();

            XSSFSpreadsheetMetadataGenerator         xssfSpreadsheetMetadataGenerator         = new XSSFSpreadsheetMetadataGenerator();
            StreamedXSSFSpreadsheetMetadataGenerator streamedXSSFSpreadsheetMetadataGenerator = new StreamedXSSFSpreadsheetMetadataGenerator();

            String rdf1 = xssfSpreadsheetMetadataGenerator.generateXSSFSpeadsheetMetadata(URI.create("http://rdf.data.org/example_xssf"), spreadsheetData);
            String rdf2 = streamedXSSFSpreadsheetMetadataGenerator.generateXSSFSpreadsheetMetadata(URI.create("http://rdf.data.org/example_xssf"), spreadsheetData);

            assertEquals("Expected identical RDF", rdf1, rdf2);
        }
        catch (IOException ioException)
        {
        	logger.log(Level.WARNING, "Problem generating Metadate", ioException);
            fail("Failed to generate Metadate");
        }
    }
}
