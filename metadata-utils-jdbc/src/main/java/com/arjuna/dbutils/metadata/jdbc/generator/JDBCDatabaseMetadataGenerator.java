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
import org.postgresql.ds.PGSimpleDataSource;

public class JDBCDatabaseMetadataGenerator
{
    private static final Logger logger = Logger.getLogger(JDBCDatabaseMetadataGenerator.class.getName());

    public String generateDatabaseToRDF(URI baseRDFURI, String databaseServerName, Integer databaseServerPort, String databaseName, String username, String password)
    {
        logger.log(Level.FINE, "Generate PostgreSQL JDBC Database Metadata");

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

            StringBuffer rdfText = new StringBuffer();

            boolean firstItem = true;
            rdfText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            rdfText.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:pg=\"http://rdfs.arjuna.com/jdbc/postgresql#\" xmlns:d=\"http://rdfs.arjuna.com/description#\">\n");

            List<UUID> tableUUIDs = new LinkedList<>();

            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet        tablesResultSet  = databaseMetaData.getTables(null, "public", null, null);
            while (tablesResultSet.next())
            {
                String tableCatalog = tablesResultSet.getString("table_cat");
                String tableSchema  = tablesResultSet.getString("table_schem");
                String tableName    = tablesResultSet.getString("table_name");
                String tableType    = tablesResultSet.getString("table_type");
                String tableRemarks = tablesResultSet.getString("remarks");
                if (tableType.equals("TABLE"))
                {
                    List<UUID> fieldUUIDs = new LinkedList<>();

                    ResultSet columnsResultSet = databaseMetaData.getColumns(tableCatalog, tableSchema, tableName, null);
                    while (columnsResultSet.next())
                    {
                        String fieldName    = columnsResultSet.getString("COLUMN_NAME");
                        String fieldType    = columnsResultSet.getString("TYPE_NAME");
                        String fieldRemarks = columnsResultSet.getString("REMARKS");

                        UUID fieldUUID = UUID.randomUUID();
                        fieldUUIDs.add(fieldUUID);

                        if (firstItem)
                            firstItem = false;
                        else
                            rdfText.append('\n');

                        rdfText.append("    <pg:Field rdf:about=\"");
                        rdfText.append(baseRDFURI.resolve('#' + fieldUUID.toString()));
                        rdfText.append("\">\n");
                        rdfText.append("        <d:hasTitle>");
                        rdfText.append(fieldName);
                        rdfText.append("</d:hasTitle>\n");
                        if (fieldRemarks != null)
                        {
                            rdfText.append("        <d:hasSummary>");
                            rdfText.append(fieldRemarks);
                            rdfText.append("</d:hasSummary>\n");
                        }
                        rdfText.append("        <pg:hasFieldName>");
                        rdfText.append(fieldName);
                        rdfText.append("</pg:hasFieldName>\n");
                        rdfText.append("        <pg:hasFieldType>");
                        rdfText.append(fieldType);
                        rdfText.append("</pg:hasFieldType>\n");
                        rdfText.append("    </pg:Field>\n");
                    }

                    UUID tableUUID = UUID.randomUUID();
                    tableUUIDs.add(tableUUID);

                    if (firstItem)
                        firstItem = false;
                    else
                        rdfText.append('\n');

                    rdfText.append("    <pg:Table rdf:about=\"");
                    rdfText.append(baseRDFURI.resolve('#' + tableUUID.toString()));
                    rdfText.append("\">\n");
                    rdfText.append("        <d:hasTitle>");
                    rdfText.append(tableName);
                    rdfText.append("</d:hasTitle>\n");
                    if (tableRemarks != null)
                    {
                        rdfText.append("        <d:hasSummary>");
                        rdfText.append(tableRemarks);
                        rdfText.append("</d:hasSummary>\n");
                    }
                    rdfText.append("        <pg:hasTableName>");
                    rdfText.append(tableName);
                    rdfText.append("</pg:hasTableName>\n");
                    rdfText.append("        <pg:hasTableField>\n            <rdf:Seq>\n");
                    for (UUID fieldUUID: fieldUUIDs)
                    {
                        rdfText.append("                <rdf:li rdf:resource=\"");
                        rdfText.append(baseRDFURI.resolve('#' + fieldUUID.toString()));
                        rdfText.append("\"/>\n");
                    }
                    rdfText.append("            </rdf:Seq>\n        </pg:hasTableField>\n");
                    rdfText.append("    </pg:Table>\n");
                }
            }

            if (firstItem)
                firstItem = false;
            else
                rdfText.append('\n');

            rdfText.append("    <pg:Database rdf:about=\"");
            rdfText.append(baseRDFURI.resolve('#' + databaseName));
            rdfText.append("\">\n");
            rdfText.append("        <d:hasTitle>");
            rdfText.append(databaseName);
            rdfText.append("</d:hasTitle>\n");
            rdfText.append("        <pg:hasDatabaseType>PostgreSQL</pg:hasDatabaseType>\n");
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
                rdfText.append("                <rdf:li rdf:resource=\"");
                rdfText.append(baseRDFURI.resolve('#' + tableUUID.toString()));
                rdfText.append("\"/>\n");
            }
            rdfText.append("            </rdf:Seq>\n       </pg:hasDatabaseTable>\n");
            rdfText.append("    </pg:Database>\n");

            rdfText.append("</rdf:RDF>\n");

            connection.close();

            if (logger.isLoggable(Level.FINE))
                logger.log(Level.FINE, "DataBase RDF:\n[\n" + rdfText.toString() + "]");

            return rdfText.toString();
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

            return null;
        }
    }
}
