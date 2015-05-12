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
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
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

    private class Column
    {
        public Column(URI aboutURI, String label, String index, String type, String title, String summary)
        {
            this.aboutURI = aboutURI;
            this.label    = label;
            this.index    = index;
            this.type     = type;
            this.title    = title;
            this.summary  = summary;
        }

        public URI    aboutURI;
        public String label;
        public String index;
        public String type;
        public String title;
        public String summary;
    }

    private class Sheet
    {
        public Sheet(URI aboutURI, String name, Map<String, Column> columns)
        {
            this.aboutURI = aboutURI;
            this.name     = name;
            this.columns  = columns;
        }

        public URI                 aboutURI;
        public String              name;
        public Map<String, Column> columns;
    }

    private class Workbook
    {
        public Workbook(URI aboutURI, Map<String, Sheet> sheets)
        {
            this.aboutURI = aboutURI;
            this.sheets   = sheets;
        }

        public URI                aboutURI;
        public Map<String, Sheet> sheets;
    }

    public String generateXSSFSpreadsheetMetadata(URI baseRDFURI, byte[] spreadsheetData)
    {
        logger.log(Level.FINE, "Generate XSSF Spreadsheet Metadata - Streamed (Data)");

        try
        {
            UUID     workbookUUID = UUID.randomUUID();
            Workbook workbook     = new Workbook(baseRDFURI.resolve('#' + workbookUUID.toString()), new HashMap<String, Sheet>());

            InputStream        xssfWorkbookInputStream = new ByteArrayInputStream(spreadsheetData);
            OPCPackage         opcPackage              = OPCPackage.open(xssfWorkbookInputStream);
            XSSFReader         xssfReader              = new XSSFReader(opcPackage);
            SharedStringsTable sharedStringsTable      = xssfReader.getSharedStringsTable();
            StylesTable        stylesTable             = xssfReader.getStylesTable();

            XMLReader      workbookParser  = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            ContentHandler workbookHandler = new WorkbookHandler(baseRDFURI, workbook);
            workbookParser.setContentHandler(workbookHandler);

            InputStream workbookInputStream = xssfReader.getWorkbookData();
            InputSource workbookSource = new InputSource(workbookInputStream);
            workbookParser.parse(workbookSource);
            workbookInputStream.close();

            XMLReader      sheetParser  = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            ContentHandler sheetHandler = new SheetHandler(sharedStringsTable, stylesTable);
            sheetParser.setContentHandler(sheetHandler);

            Iterator<InputStream> sheetInputStreamIterator = xssfReader.getSheetsData();
            while (sheetInputStreamIterator.hasNext())
            {
                InputStream sheetInputStream = sheetInputStreamIterator.next();
                InputSource sheetSource = new InputSource(sheetInputStream);
                sheetParser.parse(sheetSource);
                sheetInputStream.close();
            }
            xssfWorkbookInputStream.close();

            StringBuffer rdfText = new StringBuffer();
            generateXSSFWorkbookMetadata(rdfText, workbook);

            return rdfText.toString();
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during XSSF Spreadsheet Metadata Scan (Data)", throwable);

            return null;
        }
    }
    private class WorkbookHandler extends DefaultHandler
    {
        private static final String SPREADSHEETML_NAMESPACE = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
        private static final String RELATIONSHIPS_NAMESPACE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
        private static final String NONE_NAMESPACE          = "";
        private static final String SHEET_TAGNAME           = "sheet";
        private static final String ID_ATTRNAME             = "id";
        private static final String NAME_ATTRNAME           = "name";

        public WorkbookHandler(URI baseRDFURI, Workbook workbook)
        {
             _baseRDFURI = baseRDFURI;
             _workbook   = workbook;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {
            if ((localName != null) && localName.equals(SHEET_TAGNAME) && (uri != null) && uri.equals(SPREADSHEETML_NAMESPACE))
            {
                UUID   uuid  = UUID.randomUUID();
                String name  = attributes.getValue(NONE_NAMESPACE, NAME_ATTRNAME);
                String refId = attributes.getValue(RELATIONSHIPS_NAMESPACE, ID_ATTRNAME);

                Sheet sheet = new Sheet(_baseRDFURI.resolve('#' + uuid.toString()), name, new HashMap<String, Column>());

                _workbook.sheets.put(refId, sheet);
            }
        }

        private URI      _baseRDFURI;
        private Workbook _workbook;
    }

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

        public SheetHandler(SharedStringsTable sharedStringsTable, StylesTable stylesTable)
        {
            _sharedStringsTable = sharedStringsTable;
            _stylesTable        = stylesTable;
            _formatter          = new DataFormatter();
            _cellName           = null;
            _cellType           = null;
            _cellStyle          = null;
            _value              = new StringBuffer();
            _rowMap             = new LinkedHashMap<String, String>();
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
                    _rowMap.clear();
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

        private String removeRowNumber(String cellName)
        {
            int index = 0;
            while ((index < cellName.length()) && Character.isAlphabetic(cellName.charAt(index)))
                index++;

            return cellName.substring(0, index);
        }

        private SharedStringsTable  _sharedStringsTable;
        private StylesTable         _stylesTable;
        private DataFormatter       _formatter;
        private String              _cellName;
        private String              _cellType;
        private String              _cellStyle;
        private StringBuffer        _value;
        private Map<String, String> _rowMap;
    }

    private void generateXSSFWorkbookMetadata(StringBuffer rdfText, Workbook workbook)
    {
        logger.log(Level.FINE, "Generate XSSF Workbook Metadata");

        try
        {
            rdfText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            rdfText.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:x=\"http://rdfs.arjuna.com/xssf#\" xmlns:d=\"http://rdfs.arjuna.com/description#\">\n");

            rdfText.append("    <x:Workbook rdf:about=\"");
            rdfText.append(workbook.aboutURI);
            rdfText.append("\">\n");
            for (Sheet sheet: workbook.sheets.values())
            {
                rdfText.append("        <x:hasSheet rdf:resource=\"");
                rdfText.append(sheet.aboutURI);
                rdfText.append("\"/>\n");
            }
            rdfText.append("    </x:Workbook>\n");

            for (Sheet sheet: workbook.sheets.values())
                generateXSSFSheetMetadata(rdfText, sheet);

            rdfText.append("</rdf:RDF>\n");
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during XSSF Workbook Metadata", throwable);
        }
    }

    private void generateXSSFSheetMetadata(StringBuffer rdfText, Sheet sheet)
    {
        rdfText.append('\n');

        rdfText.append("    <x:Sheet rdf:about=\"");
        rdfText.append(sheet.aboutURI);
        rdfText.append("\">\n");
        if (sheet.name != null)
        {
            rdfText.append("        <d:hasTitle>");
            rdfText.append(sheet.name);
            rdfText.append("</d:hasTitle>\n");
        }
        for (Column column: sheet.columns.values())
        {
            rdfText.append("        <x:hasColumn rdf:resource=\"");
            rdfText.append(column.aboutURI);
            rdfText.append("\"/>\n");
        }
        rdfText.append("    </x:Sheet>\n");

        for (Column column: sheet.columns.values())
            generateXSSFColumnMetadata(rdfText, column);
    }

    private void generateXSSFColumnMetadata(StringBuffer rdfText, Column column)
    {
        rdfText.append('\n');

        rdfText.append("    <x:Column rdf:about=\"");
        rdfText.append(column.aboutURI);
        rdfText.append("\">\n");
        rdfText.append("        <x:hasLabel>");
        rdfText.append(column.label);
        rdfText.append("</x:hasLabel>\n");
        rdfText.append("        <x:hasIndex>");
        rdfText.append(column.index);
        rdfText.append("</x:hasIndex>\n");
        if (column.type != null)
        {
            rdfText.append("        <x:hasType>");
            rdfText.append(column.type);
            rdfText.append("</x:hasType>\n");
        }
        if (column.title != null)
        {
            rdfText.append("        <d:hasTitle>");
            rdfText.append(column.title);
            rdfText.append("</d:hasTitle>\n");
        }
        if (column.summary != null)
        {
            rdfText.append("        <d:hasSummary>");
            rdfText.append(column.summary);
            rdfText.append("</d:hasSummary>\n");
        }
        rdfText.append("    </x:Column>\n");
    }
}
