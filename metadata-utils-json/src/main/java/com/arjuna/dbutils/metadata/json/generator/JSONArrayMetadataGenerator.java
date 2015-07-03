/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.json.generator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONArrayMetadataGenerator
{
    private static final Logger logger = Logger.getLogger(JSONArrayMetadataGenerator.class.getName());

    public String generateJSONArrayMetadata(URI baseRDFURI, String jsonString)
    {
        logger.log(Level.FINE, "Generate JSON Array Metadata (String)");

        try
        {
            String metadata = generateJSONArrayMetadata(baseRDFURI, jsonString, null, null);

            return metadata;
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during JSON Array Metadata Scan (String)", throwable);

            return null;
        }
    }

    public String generateJSONArrayMetadata(URI baseRDFURI, byte[] jsonData)
    {
        logger.log(Level.FINE, "Generate JSON Array Metadata (Bytes)");

        try
        {
            String metadata = generateJSONArrayMetadata(baseRDFURI, new String(jsonData), null, null);

            return metadata;
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during JSON Array Metadata Scan (Bytes)", throwable);

            return null;
        }
    }

    public String generateJSONArrayMetadata(URI baseRDFURI, Map<String, Object> jsonMap)
    {
        logger.log(Level.FINE, "Generate JSON Array Metadata (Map)");

        try
        {
            String filename = (String) jsonMap.get("filename");
            String location = (String) jsonMap.get("location");
            String metadata = generateJSONArrayMetadata(baseRDFURI, (String) jsonMap.get("data"), filename, location);

            return metadata;
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during JSON Array Metadata Scan (Map)", throwable);

            return null;
        }
    }

    private String generateJSONArrayMetadata(URI baseRDFURI, String json, String filename, String location)
    {
        logger.log(Level.FINE, "Generate JSON Array Metadata");

        try
        {
            JSONArray jsonArray = new JSONArray(json);

            StringBuffer rdfText = new StringBuffer();
            rdfText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            rdfText.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:j=\"http://rdfs.arjuna.com/json#\" xmlns:d=\"http://rdfs.arjuna.com/description#\">\n");

            JSONObject jsonObject = jsonArray.getJSONObject(0);

            boolean firstItem = true;
            List<String> fieldIds = new LinkedList<String>();
            for (Object key: jsonObject.keySet())
            {
                String fieldName = jsonObject.getNames(key)[0];
                String fieldType = "string";

                String fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, fieldType);
                if (fieldId != null)
                    fieldIds.add(fieldId);
                firstItem = firstItem && fieldIds.isEmpty();
            }

            String workbookId = UUID.randomUUID().toString();
            if (! firstItem)
                rdfText.append('\n');
            else
                rdfText.append('\n');
            rdfText.append("    <j:Array rdf:about=\"");
            rdfText.append(baseRDFURI.resolve('#' + workbookId));
            rdfText.append("\">\n");
            if (filename != null)
            {
                rdfText.append("        <d:hasTitle>");
                rdfText.append(filename);
                rdfText.append("</d:hasTitle>\n");
            }
            if (location != null)
            {
                rdfText.append("        <d:hasLocation>");
                rdfText.append(location);
                rdfText.append("</d:hasLocation>\n");
            }
            for (String fieldId: fieldIds)
            {
                rdfText.append("        <j:hasField rdf:resource=\"");
                rdfText.append(baseRDFURI.resolve('#' + fieldId.toString()));
                rdfText.append("\"/>\n");
            }
            rdfText.append("    </j:Array>\n");

            rdfText.append("</rdf:RDF>\n");

            if (logger.isLoggable(Level.FINEST))
                logger.log(Level.FINEST, "JSON Array RDF:\n[\n" + rdfText.toString() + "]");

            return rdfText.toString();
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during JSON Array Metadata Scan", throwable);

            return null;
        }
    }

    private String generateJSONFieldMetadata(StringBuffer rdfText, boolean firstItem, URI baseRDFURI, String fieldName, String fieldType)
    {
        String fieldId = UUID.randomUUID().toString();

        if (firstItem)
            firstItem = false;
        else
            rdfText.append('\n');

        rdfText.append("    <j:Field rdf:about=\"");
        rdfText.append(baseRDFURI.resolve('#' + fieldId));
        rdfText.append("\">\n");
        rdfText.append("        <j:hasName>");
        rdfText.append(fieldName);
        rdfText.append("</j:hasName>\n");
        rdfText.append("        <j:hasType>");
        rdfText.append(fieldType);
        rdfText.append("</j:hasType>\n");
        rdfText.append("        <d:hasTitle>");
        rdfText.append(fieldName);
        rdfText.append("</d:hasTitle>\n");
        rdfText.append("        <d:hasSummary>");
        rdfText.append("JSON type: " + fieldType);
        rdfText.append("</d:hasSummary>\n");
        rdfText.append("    </j:Field>\n");

        return fieldId;
    }
}
