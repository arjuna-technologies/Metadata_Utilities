/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.xssf.generator;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
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

    public String generateXSSFSpeadsheetMetadata(URI baseRDFURI, File spreadsheetFile)
    {
        logger.log(Level.FINE, "Generate XSSF Speadsheet Metadata");

        try
        {
            FileInputStream xssfWorkbookInputStream = new FileInputStream(spreadsheetFile);
        	XSSFWorkbook    xssfWorkbook            = new XSSFWorkbook(xssfWorkbookInputStream);

            StringBuffer rdfText = new StringBuffer();
            rdfText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            rdfText.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:pg=\"http://rdfs.arjuna.com/xssf#\" xmlns:d=\"http://rdfs.arjuna.com/description#\">\n");

            String workbookName = UUID.randomUUID().toString();
            rdfText.append("    <x:Workbook rdf:about=\"");
            rdfText.append(baseRDFURI.resolve('#' + workbookName));
            rdfText.append("\">\n");
            for (int sheetIndex = 0; sheetIndex < xssfWorkbook.getNumberOfSheets(); sheetIndex++)
            	generateXSSFSheetMetadata(rdfText, baseRDFURI, xssfWorkbook.getSheetAt(sheetIndex));
            rdfText.append("    </x:Workbook>\n");

            rdfText.append("</rdf:RDF>\n");

            xssfWorkbookInputStream.close();

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

    private void generateXSSFSheetMetadata(StringBuffer rdfText, URI baseRDFURI, XSSFSheet sheet)
    {
        String sheetName = sheet.getSheetName();
        rdfText.append("        <x:Sheet rdf:about=\"");
        rdfText.append(baseRDFURI.resolve('#' + sheetName));
        rdfText.append("\">\n");
        XSSFRow firstRow  = sheet.getRow(0);
        XSSFRow secondRow = sheet.getRow(1);
        for (int columnIndex = sheet.getLeftCol(); columnIndex < sheet.getPhysicalNumberOfRows() + sheet.getLeftCol(); columnIndex++)
        	generateXSSFColumnMetadata(rdfText, baseRDFURI, firstRow.getCell(columnIndex), sheet.getCellComment(0, columnIndex), secondRow.getCell(columnIndex));
        rdfText.append("        </x:Sheet>\n");
    }

    private void generateXSSFColumnMetadata(StringBuffer rdfText, URI baseRDFURI, XSSFCell titleCell, XSSFComment summaryCell, XSSFCell valueCell)
    {
    	if ((titleCell != null) && (titleCell.getCellType() == XSSFCell.CELL_TYPE_STRING))
    	{
            String columnName = titleCell.getStringCellValue();
            rdfText.append("            <x:Column rdf:about=\"");
            rdfText.append(baseRDFURI.resolve('#' + columnName));
            rdfText.append("\">\n");
            rdfText.append("            </x:Column>\n");
    	}
    }
}
