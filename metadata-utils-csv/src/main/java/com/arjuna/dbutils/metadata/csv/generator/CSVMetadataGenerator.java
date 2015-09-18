/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.csv.generator;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CSVMetadataGenerator
{
    private static final Logger logger = Logger.getLogger(CSVMetadataGenerator.class.getName());

    public String generateCSVMetadata(URI baseRDFURI, String csvString)
    {
        logger.log(Level.FINE, "Generate CSV Metadata (String)");

        try
        {
            return generateCSVMetadata(baseRDFURI, csvString, null, null);
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during CSV Metadata Scan (String)", throwable);

            return null;
        }
    }

    public String generateCSVMetadata(URI baseRDFURI, byte[] csvData)
    {
        logger.log(Level.FINE, "Generate CSV Metadata (Bytes)");

        try
        {
            return generateCSVMetadata(baseRDFURI, new String(csvData), null, null);
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during CSV Metadata Scan (Bytes)", throwable);

            return null;
        }
    }

    public String generateCSVMetadata(URI baseRDFURI, Map<String, Object> csvMap)
    {
        logger.log(Level.FINE, "Generate CSV Metadata (Map)");

        try
        {
            String metadata = null;
            Object data = csvMap.get("data");
            if (data != null)
            {
                String filename = (String) csvMap.get("filename");
                String location = (String) csvMap.get("location");

                if (data instanceof byte[])
                    metadata = generateCSVMetadata(baseRDFURI, new String((byte[]) data), filename, location);
                else if (data instanceof String)
                    metadata = generateCSVMetadata(baseRDFURI, (String) data, filename, location);
            }

            return metadata;
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during CSV Metadata Scan (Map)", throwable);

            return null;
        }
    }

    private String generateCSVMetadata(URI baseRDFURI, String csv, String filename, String location)
    {
        logger.log(Level.FINE, "Generate CSV Metadata");

        try
        {
            StringBuffer rdfText = new StringBuffer();
            rdfText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            rdfText.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:j=\"http://rdfs.arjuna.com/csv#\" xmlns:d=\"http://rdfs.arjuna.com/description#\">\n");

            rdfText.append("</rdf:RDF>\n");

            if (logger.isLoggable(Level.FINEST))
                logger.log(Level.FINEST, "CSV RDF:\n[\n" + rdfText.toString() + "]");

            return rdfText.toString();
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during CSV Metadata Scan", throwable);

            return null;
        }
    }

    private String generateCSVMetadata(StringBuffer rdfText, boolean firstItem, URI baseRDFURI, String name, String csvFirstLine, String csvSecondLine)
    {
        logger.log(Level.FINE, "Generate CSV Metadata");

        return null;
    }
}
