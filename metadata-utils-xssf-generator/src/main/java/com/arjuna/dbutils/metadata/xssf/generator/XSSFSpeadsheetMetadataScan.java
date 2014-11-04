/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.xssf.generator;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;

@Stateless
public class XSSFSpeadsheetMetadataScan
{
    private static final Logger logger = Logger.getLogger(XSSFSpeadsheetMetadataScan.class.getName());

    public void scanXSSFSpeadSheet(URI baseRDFURI, String databaseServerName, Integer databaseServerPort, String databaseName, String username, String password)
    {
        logger.log(Level.FINE, "XSSF Spead Sheet Metadata Scan");

        try
        {
            StringBuffer rdfText = new StringBuffer();
            rdfText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            rdfText.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:pg=\"http://rdfs.arjuna.com/xssf#\" xmlns:d=\"http://rdfs.arjuna.com/description#\">\n");

            rdfText.append("</rdf:RDF>\n");

            logger.log(Level.FINE, "RDF:\n[\n" + rdfText.toString() + "]");
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during XSSF Spead Sheet Metadata Scan", throwable);
        }
    }
}
