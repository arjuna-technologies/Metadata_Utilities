/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.json.generator;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class JSONArrayMetadataGenerator
{
    private static final Logger logger = Logger.getLogger(JSONArrayMetadataGenerator.class.getName());

    public String generateJSONMetadata(URI baseRDFURI, String jsonString)
    {
        logger.log(Level.FINE, "Generate JSON Metadata (String)");

        try
        {
            return generateJSONMetadata(baseRDFURI, jsonString, null, null);
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during JSON Metadata Scan (String)", throwable);

            return null;
        }
    }

    public String generateJSONMetadata(URI baseRDFURI, byte[] jsonData)
    {
        logger.log(Level.FINE, "Generate JSON Metadata (Bytes)");

        try
        {
            return generateJSONMetadata(baseRDFURI, new String(jsonData), null, null);
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during JSON Metadata Scan (Bytes)", throwable);

            return null;
        }
    }

    public String generateJSONArrayMetadata(URI baseRDFURI, Map<String, Object> jsonMap)
    {
        logger.log(Level.FINE, "Generate JSON Metadata (Map)");

        try
        {
            String metadata = null;
            Object data = jsonMap.get("data");
            if (data != null)
            {
                String filename = (String) jsonMap.get("filename");
                String location = (String) jsonMap.get("location");

                if (data instanceof byte[])
                    metadata = generateJSONMetadata(baseRDFURI, new String((byte[]) data), filename, location);
                else if (data instanceof String)
                    metadata = generateJSONMetadata(baseRDFURI, (String) data, filename, location);
            }

            return metadata;
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during JSON Metadata Scan (Map)", throwable);

            return null;
        }
    }

    private String generateJSONMetadata(URI baseRDFURI, String json, String filename, String location)
    {
        logger.log(Level.FINE, "Generate JSON Metadata");

        try
        {
            StringBuffer rdfText = new StringBuffer();
            rdfText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            rdfText.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:j=\"http://rdfs.arjuna.com/json#\" xmlns:d=\"http://rdfs.arjuna.com/description#\">\n");

            JSONObject jsonObject = null;
            JSONArray  jsonArray  = null;
            try
            {
                jsonArray  = new JSONArray(json);
            }
            catch (JSONException arrayJSONException)
            {
                try
                {
                    jsonObject = new JSONObject(json);
                }
                catch (JSONException objectJSONException)
                {
                }
            }

            String fieldId = null;
            if (jsonArray != null)
                fieldId = generateJSONArrayMetadata(rdfText, true, baseRDFURI, jsonArray);
            else if (jsonObject != null)
                fieldId = generateJSONObjectMetadata(rdfText, true, baseRDFURI, jsonObject);

            if (fieldId != null)
            {
                String documentId = UUID.randomUUID().toString();
                rdfText.append("\n    <j:Document rdf:about=\"");
                rdfText.append(baseRDFURI.resolve('#' + documentId));
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
                rdfText.append("        <j:hasField rdf:resource=\"");
                rdfText.append(baseRDFURI.resolve('#' + fieldId.toString()));
                rdfText.append("\"/>\n");
                rdfText.append("    </j:Document>");
            }

            rdfText.append("</rdf:RDF>\n");

            if (logger.isLoggable(Level.FINEST))
                logger.log(Level.FINEST, "JSON RDF:\n[\n" + rdfText.toString() + "]");

            return rdfText.toString();
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during JSON Metadata Scan", throwable);

            return null;
        }
    }

    private String generateJSONArrayMetadata(StringBuffer rdfText, boolean firstItem, URI baseRDFURI, JSONArray jsonArray)
    {
        logger.log(Level.FINE, "Generate JSON Array Metadata");

        try
        {
            List<String> fieldIds = new LinkedList<String>();

            if (jsonArray.length() > 0)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                for (Object key: jsonObject.keySet())
                {
                    String fieldId   = null;
                    String fieldName = (String) key;
                    if (jsonObject.isNull(fieldName))
                        fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, "unknown");
                    else if (jsonObject.optBoolean(fieldName, false) == jsonObject.optBoolean(fieldName, true))
                        fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, "boolean");
                    else if (jsonObject.optInt(fieldName, 0) == jsonObject.optInt(fieldName, 1))
                        fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, "number");
                    else if (jsonObject.optLong(fieldName, 0) == jsonObject.optLong(fieldName, 1))
                        fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, "number");
                    else if (jsonObject.optDouble(fieldName, 0) == jsonObject.optDouble(fieldName, 1))
                        fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, "number");
                    else if (jsonObject.optString(fieldName, "X") == jsonObject.optString(fieldName, "Y"))
                        fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, "string");
                    else if (jsonObject.optJSONArray(fieldName) != null)
                        fieldId = generateJSONArrayMetadata(rdfText, firstItem, baseRDFURI, jsonObject.optJSONArray(fieldName));
                    else if (jsonObject.optJSONObject(fieldName) != null)
                        fieldId = generateJSONObjectMetadata(rdfText, firstItem, baseRDFURI, jsonObject.optJSONObject(fieldName));

                    if (fieldId != null)
                    {
                        fieldIds.add(fieldId);
                        firstItem = false;
                    }
                }
            }

            String arrayId = UUID.randomUUID().toString();
            if (! firstItem)
                rdfText.append('\n');
            else
                rdfText.append('\n');
            rdfText.append("    <j:Array rdf:about=\"");
            rdfText.append(baseRDFURI.resolve('#' + arrayId));
            rdfText.append("\">\n");
            for (String fieldId: fieldIds)
            {
                rdfText.append("        <j:hasField rdf:resource=\"");
                rdfText.append(baseRDFURI.resolve('#' + fieldId.toString()));
                rdfText.append("\"/>\n");
            }
            rdfText.append("    </j:Array>\n");

            return arrayId;
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during JSON Array Metadata Scan", throwable);

            return null;
        }
    }

    private String generateJSONObjectMetadata(StringBuffer rdfText, boolean firstItem, URI baseRDFURI, JSONObject jsonObject)
    {
        logger.log(Level.FINE, "Generate JSON Object Metadata");

        try
        {
            List<String> fieldIds = new LinkedList<String>();
            for (Object key: jsonObject.keySet())
            {
                String fieldId   = null;
                String fieldName = (String) key;
                if (jsonObject.isNull(fieldName))
                    fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, "unknown");
                else if (jsonObject.optBoolean(fieldName, false) == jsonObject.optBoolean(fieldName, true))
                    fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, "boolean");
                else if (jsonObject.optInt(fieldName, 0) == jsonObject.optInt(fieldName, 1))
                    fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, "number");
                else if (jsonObject.optLong(fieldName, 0) == jsonObject.optLong(fieldName, 1))
                    fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, "number");
                else if (jsonObject.optDouble(fieldName, 0) == jsonObject.optDouble(fieldName, 1))
                    fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, "number");
                else if (jsonObject.optString(fieldName, "X") == jsonObject.optString(fieldName, "Y"))
                    fieldId = generateJSONFieldMetadata(rdfText, firstItem, baseRDFURI, fieldName, "string");
                else if (jsonObject.optJSONArray(fieldName) != null)
                    fieldId = generateJSONArrayMetadata(rdfText, firstItem, baseRDFURI, jsonObject.optJSONArray(fieldName));
                else if (jsonObject.optJSONObject(fieldName) != null)
                    fieldId = generateJSONObjectMetadata(rdfText, firstItem, baseRDFURI, jsonObject.optJSONObject(fieldName));

                if (fieldId != null)
                {
                    fieldIds.add(fieldId);
                    firstItem = false;
                }
            }

            String objectId = UUID.randomUUID().toString();
            if (! firstItem)
                rdfText.append('\n');
            else
                rdfText.append('\n');
            rdfText.append("    <j:Object rdf:about=\"");
            rdfText.append(baseRDFURI.resolve('#' + objectId));
            rdfText.append("\">\n");
            for (String fieldId: fieldIds)
            {
                rdfText.append("        <j:hasField rdf:resource=\"");
                rdfText.append(baseRDFURI.resolve('#' + fieldId.toString()));
                rdfText.append("\"/>\n");
            }
            rdfText.append("    </j:Object>\n");

            return objectId;
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

        if (! firstItem)
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
