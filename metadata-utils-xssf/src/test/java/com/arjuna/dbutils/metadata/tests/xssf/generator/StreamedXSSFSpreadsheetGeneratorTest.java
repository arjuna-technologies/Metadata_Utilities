/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.xssf.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.junit.Test;
import com.arjuna.dbutils.metadata.xssf.generator.StreamedXSSFSpreadsheetMetadataGenerator;
import static org.junit.Assert.*;

public class StreamedXSSFSpreadsheetGeneratorTest
{
    @Test
    public void generateXSSFSpreadsheetMetadataData()
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

            StreamedXSSFSpreadsheetMetadataGenerator streamedXSSFSpeadsheetMetadataGenerator = new StreamedXSSFSpreadsheetMetadataGenerator();

            String rdf = streamedXSSFSpeadsheetMetadataGenerator.generateXSSFSpreadsheetMetadata(URI.create("http://rdf.data.org/example_xssf"), spreadsheetData);

            assertNotNull("Unexpected non null RDF", rdf);

            System.out.println("[" + rdf + "]");
        }
        catch (IOException ioException)
        {
            fail("Failed to generate Metadata");
        }
    }
}
