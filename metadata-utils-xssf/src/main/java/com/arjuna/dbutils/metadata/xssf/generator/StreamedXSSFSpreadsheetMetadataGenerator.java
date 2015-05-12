/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.xssf.generator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class StreamedXSSFSpreadsheetMetadataGenerator
{
    private static final Logger logger = Logger.getLogger(StreamedXSSFSpreadsheetMetadataGenerator.class.getName());

    public String generateXSSFSpreadsheetMetadata(URI baseRDFURI, byte[] spreadsheetData)
    {
        logger.log(Level.FINE, "Generate XSSF Spreadsheet Metadata - Streamed (Data)");

        try
        {
            Map<String, String> refIdMap = new HashMap<String, String>();

            InputStream        xssfWorkbookInputStream = new ByteArrayInputStream(spreadsheetData);
            OPCPackage         opcPackage              = OPCPackage.open(xssfWorkbookInputStream);
            XSSFReader         xssfReader              = new XSSFReader(opcPackage);
            SharedStringsTable sharedStringsTable      = xssfReader.getSharedStringsTable();
            StylesTable        stylesTable             = xssfReader.getStylesTable();

            StringBuffer rdfText = new StringBuffer();
            generateXSSFSpreadsheetMetadataStart(rdfText);

            XMLReader      workbookParser  = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            ContentHandler workbookHandler = new WorkbookHandler(rdfText, baseRDFURI, refIdMap);
            workbookParser.setContentHandler(workbookHandler);

            InputStream workbookInputStream = xssfReader.getWorkbookData();
            InputSource workbookSource = new InputSource(workbookInputStream);
            workbookParser.parse(workbookSource);
            workbookInputStream.close();

            XMLReader      sheetParser  = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            ContentHandler sheetHandler = new SheetHandler("Test", sharedStringsTable, stylesTable);
            sheetParser.setContentHandler(sheetHandler);

            Iterator<InputStream> sheetInputStreamIterator = xssfReader.getSheetsData();
            while (sheetInputStreamIterator.hasNext())
            {
                InputStream sheetInputStream = sheetInputStreamIterator.next();
                InputSource sheetSource = new InputSource(sheetInputStream);
                sheetParser.parse(sheetSource);
                sheetInputStream.close();
            }

            generateXSSFSpreadsheetMetadataEnd(rdfText);
            xssfWorkbookInputStream.close();

            return rdfText.toString();
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during XSSF Spreadsheet Metadata Scan (Data)", throwable);

            return null;
        }
    }

    private void generateXSSFSpreadsheetMetadataStart(StringBuffer rdfText)
    {
        logger.log(Level.FINE, "Generate XSSF Spreadsheet Metadata Start");

        try
        {
            rdfText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            rdfText.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:x=\"http://rdfs.arjuna.com/xssf#\" xmlns:d=\"http://rdfs.arjuna.com/description#\">\n");
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during XSSF Spreadsheet Metadata Start", throwable);
        }
    }

    private void generateXSSFSpreadsheetMetadataEnd(StringBuffer rdfText)
    {
        logger.log(Level.FINE, "Generate XSSF Spreadsheet Metadata End");

        try
        {
            rdfText.append("</rdf:RDF>\n");
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during XSSF Spreadsheet Metadata Scan", throwable);
        }
    }

    private String generateXSSFSheetMetadata(StringBuffer rdfText, URI baseRDFURI, String sheetName)
    {
        String sheetId = UUID.randomUUID().toString();
        rdfText.append("    <x:Sheet rdf:about=\"");
        rdfText.append(baseRDFURI.resolve('#' + sheetId));
        rdfText.append("\">\n");
        if (sheetName != null)
        {
            rdfText.append("        <d:hasTitle>");
            rdfText.append(sheetName);
            rdfText.append("</d:hasTitle>\n");
        }
//        for (String columnId: columnIds)
//        {
//            rdfText.append("        <x:hasColumn rdf:resource=\"");
//            rdfText.append(baseRDFURI.resolve('#' + columnId.toString()));
//            rdfText.append("\"/>\n");
//        }
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

    private class WorkbookHandler extends DefaultHandler
    {
        private static final String SPREADSHEETML_NAMESPACE = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
        private static final String NONE_NAMESPACE          = "";
        private static final String SHEET_TAGNAME           = "sheet";
        private static final String NAME_ATTRNAME           = "name";

        public WorkbookHandler(StringBuffer rdfText, URI baseRDFURI, Map<String, String> refIdMap)
        {
             _rdfText    = rdfText;
             _baseRDFURI = baseRDFURI;
             _refIdMap   = refIdMap;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {
            if ((localName != null) && localName.equals(SHEET_TAGNAME) && (uri != null) && uri.equals(SPREADSHEETML_NAMESPACE))
            {
                String name = attributes.getValue(NONE_NAMESPACE, NAME_ATTRNAME);

                generateXSSFSheetMetadata(_rdfText, _baseRDFURI, name);
            }
        }

        private StringBuffer        _rdfText;
        private URI                 _baseRDFURI;
        private Map<String, String> _refIdMap;
    }

    private static final String[] KEYS = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                                           "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                                           "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM",
                                           "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU" };

    private class SheetHandler extends DefaultHandler
    {
        private static final String SPREADSHEETML_NAMESPACE = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
        private static final String NONE_NAMESPACE          = "";
        private static final String ROW_TAGNAME             = "row";
        private static final String CELL_TAGNAME            = "c";
        private static final String VALUE_TAGNAME           = "v";
        private static final String SHEETDATA_TAGNAME       = "sheetData";
        private static final String REF_ATTRNAME            = "r";
        private static final String TYPE_ATTRNAME           = "t";
        private static final String STYLE_ATTRNAME          = "s";

        public SheetHandler(String tableName, SharedStringsTable sharedStringsTable, StylesTable stylesTable)
        {
            _tableName          = tableName;
            _sharedStringsTable = sharedStringsTable;
            _stylesTable        = stylesTable;
            _formatter          = new DataFormatter();
            _cellName           = null;
            _cellType           = null;
            _cellStyle          = null;
            _value              = new StringBuffer();
            _rowMap             = new LinkedHashMap<String, String>();
            _rowCount           = 0;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {
            try
            {
                if ((localName != null) && localName.equals(CELL_TAGNAME) && (uri != null) && uri.equals(SPREADSHEETML_NAMESPACE))
                {
                    _cellName  = attributes.getValue(NONE_NAMESPACE, REF_ATTRNAME);
                    _cellType  = attributes.getValue(NONE_NAMESPACE, TYPE_ATTRNAME);
                    _cellStyle = attributes.getValue(NONE_NAMESPACE, STYLE_ATTRNAME);
                }
                else if ((localName != null) && localName.equals(VALUE_TAGNAME) && (uri != null) && uri.equals(SPREADSHEETML_NAMESPACE))
                    _value.setLength(0);
            }
            catch (Throwable throwable)
            {
                logger.log(Level.WARNING, "Problem processing start tag", throwable);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            try
            {
                if ((localName != null) && localName.equals(VALUE_TAGNAME) && (uri != null) && uri.equals(SPREADSHEETML_NAMESPACE))
                {
                    if (_cellType == null)
                    {
                        try
                        {
                            int           styleIndex   = Integer.parseInt(_cellStyle);
                            XSSFCellStyle style        = _stylesTable.getStyleAt(styleIndex);
                            short         formatIndex  = style.getDataFormat();
                            String        formatString = style.getDataFormatString();
                            if (formatString == null)
                                formatString = BuiltinFormats.getBuiltinFormat(formatIndex);
                            String text = _formatter.formatRawCellContents(Double.parseDouble(_value.toString()), formatIndex, formatString);
                            _rowMap.put(removeRowNumber(_cellName), text);
                        }
                        catch (NumberFormatException numberFormatException)
                        {
                            logger.log(Level.WARNING, "Failed to parse 'Style' index", numberFormatException);
                        }
                        catch (IndexOutOfBoundsException indexOutOfBoundsException)
                        {
                            logger.log(Level.WARNING, "Failed to find 'Style'", indexOutOfBoundsException);
                        }
                    }
                    else if (_cellType.equals("n"))
                        _rowMap.put(removeRowNumber(_cellName), _value.toString());
                    else if (_cellType.equals("s"))
                    {
                        String sharedStringsTableIndex = _value.toString();
                        try
                        {
                            int index = Integer.parseInt(sharedStringsTableIndex);
                            XSSFRichTextString rtss = new XSSFRichTextString(_sharedStringsTable.getEntryAt(index));
                            _rowMap.put(removeRowNumber(_cellName), rtss.toString());
                        }
                        catch (NumberFormatException numberFormatException)
                        {
                            logger.log(Level.WARNING, "Failed to parse 'Shared Strings Table' index '" + sharedStringsTableIndex + "'", numberFormatException);
                        }
                        catch (IndexOutOfBoundsException indexOutOfBoundsException)
                        {
                            logger.log(Level.WARNING, "Failed to find 'Shared String' - '" + sharedStringsTableIndex + "'", indexOutOfBoundsException);
                        }
                    }
                    else
                        logger.log(Level.WARNING, "Unsupported cell type '" + _cellType + "'");

                    _value.setLength(0);
                }
                else if ((localName != null) && localName.equals(ROW_TAGNAME) && (uri != null) && uri.equals(SPREADSHEETML_NAMESPACE))
                {
                    String sql = rowMap2SQL(_rowMap);
                    if (logger.isLoggable(Level.FINE))
                        logger.log(Level.FINE, "SQL: [" + sql + "]");

                    _rowMap.clear();
                }
            }
            catch (Throwable throwable)
            {
                logger.log(Level.WARNING, "Problem processing end tag", throwable);
            }
        }

        @Override
        public void characters(char[] characters, int start, int length)
            throws SAXException
        {
            try
            {
                _value.append(characters, start, length);
            }
            catch (Throwable throwable)
            {
                logger.log(Level.WARNING, "Problem processing characters", throwable);
            }
        }

        private String rowMap2SQL(Map<String, String> rowMap)
        {
            StringBuffer sql = new StringBuffer();

            sql.append("INSERT INTO ");
            sql.append(_tableName);
            sql.append(" VALUES (");
            boolean first = true;
            for (String key: KEYS)
            {
                if (! first)
                    sql.append(',');
                else
                    first = false;

                String value = rowMap.get(key);

                sql.append('\'');
                if (value != null)
                    sql.append(sqlEscape(value));
                sql.append('\'');
            }
            sql.append(",'',");
            sql.append(Long.toString(_rowCount));
            sql.append(");");

            return sql.toString();
        }

        private String removeRowNumber(String cellName)
        {
            int index = 0;
            while ((index < cellName.length()) && Character.isAlphabetic(cellName.charAt(index)))
                index++;

            return cellName.substring(0, index);
        }

        private String sqlEscape(String sql)
        {
//            return sql.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'");
            return sql.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "");
        }

        private String              _tableName;
        private SharedStringsTable  _sharedStringsTable;
        private StylesTable         _stylesTable;
        private DataFormatter       _formatter;
        private String              _cellName;
        private String              _cellType;
        private String              _cellStyle;
        private StringBuffer        _value;
        private Map<String, String> _rowMap;
        private long                _rowCount;
    }
}
