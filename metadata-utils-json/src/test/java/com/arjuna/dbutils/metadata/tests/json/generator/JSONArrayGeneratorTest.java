/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.json.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.junit.Test;
import com.arjuna.dbutils.metadata.json.generator.JSONArrayMetadataGenerator;

import static org.junit.Assert.*;

public class JSONArrayGeneratorTest
{
    @Test
    public void generateJSONArrayMetadataData()
    {
        try
        {
            File         jsonArrayFile        = new File("Test01.json");
            byte[]       jsonArrayData        = new byte[(int) jsonArrayFile.length()];
            InputStream  jsonArrayInputStream = new FileInputStream(jsonArrayFile);
            int          character            = jsonArrayInputStream.read();
            int          index                = 0;
            while (character != -1)
            {
                jsonArrayData[index] = (byte) character;
                character = jsonArrayInputStream.read();
                index++;
            }
            jsonArrayInputStream.close();

            JSONArrayMetadataGenerator jsonArrayMetadataGenerator = new JSONArrayMetadataGenerator();

            String rdf = jsonArrayMetadataGenerator.generateJSONMetadata(URI.create("http://rdf.data.org/example_json"), jsonArrayData);

            assertNotNull("Unexpected non null RDF", rdf);

//            System.out.println("[" + rdf + "]");
        }
        catch (IOException ioException)
        {
            fail("Failed to generate Metadata");
        }
    }
}
