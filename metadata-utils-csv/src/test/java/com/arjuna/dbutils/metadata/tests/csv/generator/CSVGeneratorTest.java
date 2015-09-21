/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.csv.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.junit.Test;
import com.arjuna.dbutils.metadata.csv.generator.CSVMetadataGenerator;

import static org.junit.Assert.*;

public class CSVGeneratorTest
{
    @Test
    public void generateCSVMetadataData()
    {
        try
        {
            File        csvFile        = new File("Test01.csv");
            byte[]      csvData        = new byte[(int) csvFile.length()];
            InputStream csvInputStream = new FileInputStream(csvFile);
            int         character      = csvInputStream.read();
            int         index          = 0;
            while (character != -1)
            {
                csvData[index] = (byte) character;
                character = csvInputStream.read();
                index++;
            }
            csvInputStream.close();

            CSVMetadataGenerator csvMetadataGenerator = new CSVMetadataGenerator();

            String rdf = csvMetadataGenerator.generateCSVMetadata(URI.create("http://rdf.data.org/example_csv"), csvData);

            assertNotNull("Unexpected non null RDF", rdf);

            System.out.println("[" + rdf + "]");
        }
        catch (IOException ioException)
        {
            fail("Failed to generate Metadata");
        }
    }
}
