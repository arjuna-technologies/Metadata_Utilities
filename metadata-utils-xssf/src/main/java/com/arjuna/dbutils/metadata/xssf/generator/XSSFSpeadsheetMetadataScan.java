/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.xssf.generator;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XSSFSpeadsheetMetadataScan
{
    private static final Logger logger = Logger.getLogger(XSSFSpeadsheetMetadataScan.class.getName());

    public String generateXSSFSpeadsheetMetadata(URI baseRDFURI)
    {
        logger.log(Level.FINE, "Generate XSSF Speadsheet Metadata");

        try
        {
            StringBuffer rdfText = new StringBuffer();
            rdfText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            rdfText.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:pg=\"http://rdfs.arjuna.com/xssf#\" xmlns:d=\"http://rdfs.arjuna.com/description#\">\n");

            rdfText.append("</rdf:RDF>\n");

            if (logger.isLoggable(Level.FINE))
                logger.log(Level.FINE, "XSSF Speadsheet RDF:\n[\n" + rdfText.toString() + "]");

            return rdfText.toString();
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during XSSF Speadsheet Metadata Scan", throwable);
            
            return null;
        }
    }
}