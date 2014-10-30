/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.jdbc.generator;

import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;

import org.postgresql.ds.PGSimpleDataSource;

@Stateless
public class JDBCDatabaseMetadataScan
{
    private static final Logger logger = Logger.getLogger(JDBCDatabaseMetadataScan.class.getName());

    public void scanDataBase(URI baseRDFURI, String databaseServerName, Integer databaseServerPort, String databaseName, String username, String password)
    {
        logger.log(Level.FINE, "PostgreSQL JDBC Database Metadata Scan");

        Connection connection = null;
        try
        {
            PGSimpleDataSource dataSource = new PGSimpleDataSource();
            dataSource.setServerName(databaseServerName);
            dataSource.setPortNumber(databaseServerPort);
            dataSource.setDatabaseName(databaseName);
            dataSource.setUser(username);
            dataSource.setPassword(password);

            connection = dataSource.getConnection();

            boolean      firstItem = true;
            StringBuffer rdfText   = new StringBuffer();
            rdfText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            rdfText.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:pg=\"http://rdfs.arjuna.com/jdbc/postgresql#\">\n");

            List<UUID> tableUUIDs = new LinkedList<>();

            DatabaseMetaData databaseMetaData   = connection.getMetaData();
            ResultSet        allTablesResultSet = databaseMetaData.getTables(null, "public", null, null);
            while (allTablesResultSet.next())
            {
                String tableName    = allTablesResultSet.getString("table_name");
                String tableType    = allTablesResultSet.getString("table_type");
                String tableRemarks = allTablesResultSet.getString("remarks");
                if (tableType.equals("TABLE"))
                {
                	if (firstItem)
                		firstItem = false;
                	else
                        rdfText.append('\n');

                	UUID tableUUID = UUID.randomUUID();
                	tableUUIDs.add(tableUUID);

                	rdfText.append("    <pg:Table rdf:about=\"");
                    rdfText.append(baseRDFURI.resolve('#' + tableUUID.toString()));
                    rdfText.append("\">\n");
                    rdfText.append("        <pg:hasTableName>");
                    rdfText.append(tableName);
                    rdfText.append("</pg:hasTableName>\n");
                    rdfText.append("        <pg:hasTableField>\n            <rdf:Seq>\n");
                    rdfText.append("TO DO\n");
                    rdfText.append("            </rdf:Seq>\n       </pg:hasTableField>\n");
                    rdfText.append("    </pg:Table>\n");

                    ResultSet allColumnsResultSet = databaseMetaData.getColumns(allTablesResultSet.getString("table_cat"), allTablesResultSet.getString("table_schem"), allTablesResultSet.getString("table_name"), null);
                    while (allColumnsResultSet.next())
                    {
                        String columnName     = allColumnsResultSet.getString("COLUMN_NAME");
                        String columnNRemarks = allColumnsResultSet.getString("REMARKS");
                    }
                }
            }

        	if (firstItem)
        		firstItem = false;
        	else
                rdfText.append('\n');

        	rdfText.append("    <pg:Database rdf:about=\"");
            rdfText.append(baseRDFURI.resolve('#' + databaseName));
            rdfText.append("\">\n");
            rdfText.append("        <pg:hasDatabaseType>PostgreSQL</pg:hasDatabaseName>\n");
            rdfText.append("        <pg:hasDatabaseHostName>");
            rdfText.append(databaseServerName);
            rdfText.append("</pg:hasDatabaseHostName>\n");
            rdfText.append("        <pg:hasDatabasePortNumber>");
            rdfText.append(databaseServerPort);
            rdfText.append("</pg:hasDatabasePortNumber>\n");
            rdfText.append("        <pg:hasDatabaseUserName>");
            rdfText.append(username);
            rdfText.append("</pg:hasDatabaseUserName>\n");
            rdfText.append("        <pg:hasDatabasePassword>");
            rdfText.append(password);
            rdfText.append("</pg:hasDatabasePassword>\n");
            rdfText.append("        <pg:hasDatabaseName>");
            rdfText.append(databaseName);
            rdfText.append("</pg:hasDatabaseName>\n");
            rdfText.append("        <pg:hasDatabaseTable>\n            <rdf:Seq>\n");
            for (UUID tableUUID: tableUUIDs)
            {
            	rdfText.append("                <rdf:li>\n");
            	rdfText.append("                    <pg:hasDatabaseTable rdf:resource=\"");
            	rdfText.append(baseRDFURI.resolve('#' + tableUUID.toString()));
            	rdfText.append("\"/>\n");
            	rdfText.append("                </rdf:li>\n");
            }
            rdfText.append("            </rdf:Seq>\n       </pg:hasDatabaseTable>\n");
            rdfText.append("    </pg:Database>\n");
            
            rdfText.append("</rdf:RDF>\n");

            connection.close();

            logger.log(Level.FINE, "RDF:\n[\n" + rdfText.toString() + "]");
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during JDBC Database Metadata Scan", throwable);

            try
            {
                if (! connection.isClosed())
                    connection.close();
            }
            catch (Throwable sqlThrowable)
            {
                logger.log(Level.WARNING, "Problem Generating during JDBC Database Metadata Scan, close", sqlThrowable);
            }
        }
    }
}
