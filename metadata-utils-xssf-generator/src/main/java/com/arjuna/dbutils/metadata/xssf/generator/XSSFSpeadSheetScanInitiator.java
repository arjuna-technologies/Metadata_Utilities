/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.xssf.generator;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class XSSFSpeadSheetScanInitiator
{
    private static final Logger logger = Logger.getLogger(XSSFSpeadSheetScanInitiator.class.getName());

    @PostConstruct
    public void setup()
    {
        logger.log(Level.FINE, "PostgreSQL JDBC Database Metadata Scan Startup");

        URI baseRDFURI = URI.create("http://rdf.example.org/PS_Test");
        _jdbcDatabaseMetadataScan.scanXSSFSpeadSheet(baseRDFURI, "localhost", 0, "databroker", "test_user", "test_password");
    }

    @EJB
    private XSSFSpeadSheetMetadataScan _jdbcDatabaseMetadataScan;
}
