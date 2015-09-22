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
import static org.apache.commons.lang3.StringEscapeUtils.escapeXml10;

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
            List<String> columnNames = extractColumnNames(csv);

            StringBuffer rdfText = new StringBuffer();
            rdfText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            rdfText.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:c=\"http://rdfs.arjuna.com/csv#\" xmlns:d=\"http://rdfs.arjuna.com/description#\">\n");

            boolean firstItem = true;

            List<String> columnIds = new LinkedList<String>();
            for (int columnIndex = 0; columnIndex < columnNames.size(); columnIndex++)
            {
                String columnId = generateCSVFieldMetadata(rdfText, firstItem, baseRDFURI, columnNames.get(columnIndex));
                if (columnId != null)
                    columnIds.add(columnId);
                firstItem = firstItem && columnIds.isEmpty();
            }

            String tableId = UUID.randomUUID().toString();
            if (! firstItem)
                rdfText.append('\n');
            else
                rdfText.append('\n');
            rdfText.append("    <c:Table rdf:about=\"");
            rdfText.append(baseRDFURI.resolve('#' + tableId));
            rdfText.append("\">\n");
            if (filename != null)
            {
                rdfText.append("        <d:hasTitle>");
                rdfText.append(escapeXml10(filename));
                rdfText.append("</d:hasTitle>\n");
            }
            if (location != null)
            {
                rdfText.append("        <d:hasLocation>");
                rdfText.append(escapeXml10(location));
                rdfText.append("</d:hasLocation>\n");
            }
            for (String columnId: columnIds)
            {
                rdfText.append("        <c:hasField rdf:resource=\"");
                rdfText.append(baseRDFURI.resolve('#' + columnId.toString()));
                rdfText.append("\"/>\n");
            }
            rdfText.append("    </c:Table>\n");

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

    private String generateCSVFieldMetadata(StringBuffer rdfText, boolean firstItem, URI baseRDFURI, String columnName)
    {
        if (! firstItem)
            rdfText.append('\n');

        String columnId  = UUID.randomUUID().toString();
        rdfText.append("    <c:Column rdf:about=\"");
        rdfText.append(baseRDFURI.resolve('#' + columnId));
        rdfText.append("\">\n");
        if (columnName != null)
        {
            rdfText.append("        <d:hasTitle>");
            rdfText.append(escapeXml10(columnName));
            rdfText.append("</d:hasTitle>\n");
            rdfText.append("        <c:hasColumnName>");
            rdfText.append(escapeXml10(columnName));
            rdfText.append("</c:hasColumnName>\n");
        }
        rdfText.append("    </c:Column>\n");

        return columnId;
    }

    private List<String> extractColumnNames(String csv)
    {
        List<String> columnNames = new LinkedList<String>();

        int     lastStart = 0;
        boolean firstLine = true;
        for (int index = 0; firstLine && (index < csv.length()); index++)
        {
            char character = csv.charAt(index);

            if ((character == ',') || (character == '\n') || (character == '\r'))
            {
                columnNames.add(csv.substring(lastStart, index).trim());

                lastStart = index + 1;
            }

            firstLine = (character != '\n') && (character != '\r');
        }

        return columnNames;
    }
}
