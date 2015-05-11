/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.xssf.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import org.junit.Test;
import com.arjuna.dbutils.metadata.xssf.generator.StreamedXSSFSpeadsheetMetadataGenerator;
import static org.junit.Assert.*;

public class StreamedXSSFSpreadsheetGeneratorTest
{
    @Test
    public void generateXSSFSpeadsheetMetadataData()
    {
        try
        {
            File         spreadsheetFile        = new File("Test01.xlsx");
            byte[]       spreadsheetData        = new byte[(int) spreadsheetFile.length()];
            InputStream  spreadsheetInputStream = new FileInputStream(spreadsheetFile);
            int          character              = spreadsheetInputStream.read();
            int          index                  = 0;
            while (character != -1)
            {
                spreadsheetData[index] = (byte) character;
                character = spreadsheetInputStream.read();
                index++;
            }
            spreadsheetInputStream.close();

            StreamedXSSFSpeadsheetMetadataGenerator streamedXSSFSpeadsheetMetadataGenerator = new StreamedXSSFSpeadsheetMetadataGenerator();

            String rdf = streamedXSSFSpeadsheetMetadataGenerator.generateXSSFSpeadsheetMetadata(URI.create("http://rdf.data.org/example_xssf"), spreadsheetData);

            System.out.println("[\n" + rdf + "\n]");

            assertNotNull("Unexpected non null RDF", rdf);
        }
        catch (Throwable throwable)
        {
            fail("Failed to generate Metadata Inventory");
        }
    }
}
