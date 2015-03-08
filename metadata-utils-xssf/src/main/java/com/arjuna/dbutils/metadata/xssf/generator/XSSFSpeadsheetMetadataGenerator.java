/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.xssf.generator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XSSFSpeadsheetMetadataGenerator
{
    private static final Logger logger = Logger.getLogger(XSSFSpeadsheetMetadataGenerator.class.getName());

    public String generateXSSFSpeadsheetMetadata(URI baseRDFURI, String spreadsheetData)
    {
        logger.log(Level.FINE, "Generate XSSF Speadsheet Metadata (Data)");

        try
        {
            InputStream xssfWorkbookInputStream = new ByteArrayInputStream(spreadsheetData.getBytes());
            String      metadata                = generateXSSFSpeadsheetMetadata(baseRDFURI, xssfWorkbookInputStream);
            xssfWorkbookInputStream.close();

            return metadata;
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during XSSF Speadsheet Metadata Scan (File)", throwable);

            return null;
        }
    }

    public String generateXSSFSpeadsheetMetadata(URI baseRDFURI, File spreadsheetFile)
    {
        logger.log(Level.FINE, "Generate XSSF Speadsheet Metadata (File)");

        try
        {
            InputStream xssfWorkbookInputStream = new FileInputStream(spreadsheetFile);
            String      metadata                = generateXSSFSpeadsheetMetadata(baseRDFURI, xssfWorkbookInputStream);
            xssfWorkbookInputStream.close();

            return metadata;
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during XSSF Speadsheet Metadata Scan (File)", throwable);

            return null;
        }
    }

    private String generateXSSFSpeadsheetMetadata(URI baseRDFURI, InputStream xssfWorkbookInputStream)
    {
        logger.log(Level.FINE, "Generate XSSF Speadsheet Metadata");

        try
        {
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(xssfWorkbookInputStream);

            StringBuffer rdfText = new StringBuffer();
            rdfText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            rdfText.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:pg=\"http://rdfs.arjuna.com/xssf#\" xmlns:d=\"http://rdfs.arjuna.com/description#\">\n");

            boolean firstItem = true;

            List<String> sheetIds = new LinkedList<String>();
            for (int sheetIndex = 0; sheetIndex < xssfWorkbook.getNumberOfSheets(); sheetIndex++)
            {
                String sheetId = generateXSSFSheetMetadata(rdfText, firstItem, baseRDFURI, xssfWorkbook.getSheetAt(sheetIndex));
                if (sheetId != null)
                    sheetIds.add(sheetId);
                firstItem = firstItem && sheetIds.isEmpty();
            }

            String workbookId = UUID.randomUUID().toString();
            if (! firstItem)
                rdfText.append('\n');
            else
                rdfText.append('\n');
            rdfText.append("    <x:Workbook rdf:about=\"");
            rdfText.append(baseRDFURI.resolve('#' + workbookId));
            rdfText.append("\">\n");
            for (String sheetId: sheetIds)
            {
                rdfText.append("        <x:hasSheet rdf:resource=\"");
                rdfText.append(baseRDFURI.resolve('#' + sheetId.toString()));
                rdfText.append("\"/>\n");
            }
            rdfText.append("    </x:Workbook>\n");

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

    private String generateXSSFSheetMetadata(StringBuffer rdfText, boolean firstItem, URI baseRDFURI, XSSFSheet sheet)
    {
        XSSFRow      firstRow  = sheet.getRow(0);
        XSSFRow      secondRow = sheet.getRow(1);
        List<String> columnIds = new LinkedList<String>();
        for (int columnIndex = sheet.getLeftCol(); columnIndex < sheet.getPhysicalNumberOfRows() + sheet.getLeftCol(); columnIndex++)
        {
            String columnId = generateXSSFColumnMetadata(rdfText, firstItem, baseRDFURI, columnIndex, firstRow.getCell(columnIndex), sheet.getCellComment(0, columnIndex), secondRow.getCell(columnIndex));
            if (columnId != null)
                columnIds.add(columnId);
            firstItem = firstItem && columnIds.isEmpty();
        }

        if (! firstItem)
            rdfText.append('\n');

        String sheetId   = UUID.randomUUID().toString();
        String sheetName = sheet.getSheetName();
        rdfText.append("    <x:Sheet rdf:about=\"");
        rdfText.append(baseRDFURI.resolve('#' + sheetId));
        rdfText.append("\">\n");
        if (sheetName != null)
        {
            rdfText.append("        <d:hasTitle>");
            rdfText.append(sheetName);
            rdfText.append("</d:hasTitle>\n");
        }
        for (String columnId: columnIds)
        {
            rdfText.append("        <x:hasColumn rdf:resource=\"");
            rdfText.append(baseRDFURI.resolve('#' + columnId.toString()));
            rdfText.append("\"/>\n");
        }
        rdfText.append("    </x:Sheet>\n");

        return sheetId;
    }

    private String generateXSSFColumnMetadata(StringBuffer rdfText, boolean firstItem, URI baseRDFURI, int columnIndex, XSSFCell titleCell, XSSFComment summaryCell, XSSFCell valueCell)
    {
        if ((titleCell != null) && (titleCell.getCellType() == XSSFCell.CELL_TYPE_STRING))
        {
            String columnId      = UUID.randomUUID().toString();
            String columnLabel   = removeRowNumber(titleCell.getReference());
            String columnName    = titleCell.getStringCellValue();
            String columnComment = null;
            if (titleCell.getCellComment() != null)
                columnComment = titleCell.getCellComment().getString().getString();
            String columnType = null;
            if (valueCell != null)
            {
                if (valueCell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC)
                    columnType =  "Number";
                else if (valueCell.getCellType() == XSSFCell.CELL_TYPE_STRING)
                    columnType =  "String";
                else if (valueCell.getCellType() == XSSFCell.CELL_TYPE_BOOLEAN)
                    columnType =  "Boolean";
            }

            if (firstItem)
                firstItem = false;
            else
                rdfText.append('\n');

            rdfText.append("    <x:Column rdf:about=\"");
            rdfText.append(baseRDFURI.resolve('#' + columnId));
            rdfText.append("\">\n");
            rdfText.append("        <x:hasLabel>");
            rdfText.append(columnLabel);
            rdfText.append("</x:hasLabel>\n");
            rdfText.append("        <x:hasIndex>");
            rdfText.append(columnIndex);
            rdfText.append("</x:hasIndex>\n");
            if (columnType != null)
            {
                rdfText.append("        <x:hasType>");
                rdfText.append(columnType);
                rdfText.append("</x:hasType>\n");
            }
            if (columnName != null)
            {
                rdfText.append("        <d:hasTitle>");
                rdfText.append(columnName);
                rdfText.append("</d:hasTitle>\n");
            }
            if (columnComment != null)
            {
                rdfText.append("        <d:hasSummary>");
                rdfText.append(columnComment);
                rdfText.append("</d:hasSummary>\n");
            }
            rdfText.append("    </x:Column>\n");

            return columnId;
        }
        else
            return null;
    }

    private String removeRowNumber(String cellName)
    {
        int index = 0;
        while ((index < cellName.length()) && Character.isAlphabetic(cellName.charAt(index)))
            index++;

        return cellName.substring(0, index);
    }
}
