/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.xssf.generator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
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
        public Sheet(URI aboutURI, String name, String sheetId, String refId, Map<String, Column> columns)
        {
            this.aboutURI = aboutURI;
            this.name     = name;
            this.sheetId  = sheetId;
            this.refId    = refId;
            this.columns  = columns;
        }

        public URI                 aboutURI;
        public String              name;
        public String              sheetId;
        public String              refId;
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
            InputStream xssfWorkbookInputStream = new ByteArrayInputStream(spreadsheetData);
            OPCPackage  opcPackage              = OPCPackage.open(xssfWorkbookInputStream);
            XSSFReader  xssfReader              = new XSSFReader(opcPackage);
            StylesTable stylesTable             = xssfReader.getStylesTable();

            UUID     workbookUUID = UUID.randomUUID();
            Workbook workbook     = new Workbook(baseRDFURI.resolve('#' + workbookUUID.toString()), new HashMap<String, Sheet>());

            XMLReader      workbookParser  = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            ContentHandler workbookHandler = new WorkbookHandler(baseRDFURI, workbook);
            workbookParser.setContentHandler(workbookHandler);

            InputStream workbookInputStream = xssfReader.getWorkbookData();
            InputSource workbookSource = new InputSource(workbookInputStream);
            workbookParser.parse(workbookSource);
            workbookInputStream.close();

            for (Sheet sheet: workbook.sheets.values())
            {
                XMLReader      sheetParser  = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
                ContentHandler sheetHandler = new SheetHandler(baseRDFURI, stylesTable, sheet);
                sheetParser.setContentHandler(sheetHandler);

                InputStream sheetInputStream = xssfReader.getSheet(sheet.refId);
                InputSource sheetSource = new InputSource(sheetInputStream);
                sheetParser.parse(sheetSource);
                sheetInputStream.close();
            }
            xssfWorkbookInputStream.close();

            XMLReader      sharedStringsParser  = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            ContentHandler sharedStringsHandler = new SharedStringsHandler(workbook);
            sharedStringsParser.setContentHandler(sharedStringsHandler);

            InputStream sharedStringsInputStream = xssfReader.getSharedStringsData();
            InputSource sharedStringsSource = new InputSource(sharedStringsInputStream);
            sharedStringsParser.parse(sharedStringsSource);
            sharedStringsInputStream.close();

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
        private static final String NAME_ATTRNAME           = "name";
        private static final String SHEETID_ATTRNAME        = "sheetId";
        private static final String ID_ATTRNAME             = "id";

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
                UUID   uuid    = UUID.randomUUID();
                String name    = attributes.getValue(NONE_NAMESPACE, NAME_ATTRNAME);
                String sheetId = attributes.getValue(NONE_NAMESPACE, SHEETID_ATTRNAME);
                String refId   = attributes.getValue(RELATIONSHIPS_NAMESPACE, ID_ATTRNAME);

                Sheet sheet = new Sheet(_baseRDFURI.resolve('#' + uuid.toString()), name, sheetId, refId, new HashMap<String, Column>());

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
        private static final String CELL_TAGNAME            = "c";
        private static final String VALUE_TAGNAME           = "v";
        private static final String REF_ATTRNAME            = "r";
        private static final String TYPE_ATTRNAME           = "t";
        private static final String STYLE_ATTRNAME          = "s";

        public SheetHandler(URI baseRDFURI, StylesTable stylesTable, Sheet sheet)
        {
            _baseRDFURI  = baseRDFURI;
            _stylesTable = stylesTable;
            _sheet       = sheet;

            _cellName  = null;
            _cellType  = null;
            _cellStyle = null;
            _value     = new StringBuffer();
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
                    System.out.println("Cell: [" + _cellName + "][" + getColumnLabel(_cellName) + "-" + getRowNumber(_cellName) + "][" + _cellType + "][" + _cellStyle + "]\"" + _value + "\"");

                    String rowNumber = getRowNumber(_cellName);
                    if ("1".equals(rowNumber))
                    {
                        UUID   uuid        = UUID.randomUUID();
                        String columnLabel = getColumnLabel(_cellName);

                        Column column = new Column(_baseRDFURI.resolve('#' + uuid.toString()), columnLabel, null, null, null, null);

                        _sheet.columns.put(columnLabel, column);
                    }

                    _value.setLength(0);
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

        private URI         _baseRDFURI;
        private Sheet       _sheet;
        private StylesTable _stylesTable;

        private String        _cellName;
        private String        _cellType;
        private String        _cellStyle;
        private StringBuffer  _value;
    }

    private class CommentsHandler extends DefaultHandler
    {
        private static final String SPREADSHEETML_NAMESPACE = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
        private static final String NONE_NAMESPACE          = "";
        private static final String COMMENT_TAGNAME         = "comment";
        private static final String T_TAGNAME               = "t";
        private static final String REF_ATTRNAME            = "ref";

        public CommentsHandler(Sheet sheet)
        {
             _sheet = sheet;
             _ref   = null;
             _value = new StringBuffer();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {
            if ((localName != null) && localName.equals(COMMENT_TAGNAME) && (uri != null) && uri.equals(SPREADSHEETML_NAMESPACE))
                _ref = attributes.getValue(NONE_NAMESPACE, REF_ATTRNAME);
            else if ((localName != null) && localName.equals(T_TAGNAME) && (uri != null) && uri.equals(SPREADSHEETML_NAMESPACE))
                _value.setLength(0);
        }

        @Override
        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            if ((localName != null) && localName.equals(T_TAGNAME) && (uri != null) && uri.equals(SPREADSHEETML_NAMESPACE))
            {
                String value = _value.toString();

                System.out.println("C{{{" + value + "}}}}");
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

        private Sheet        _sheet;
        private String       _ref;
        private StringBuffer _value;
    }

    private class SharedStringsHandler extends DefaultHandler
    {
        private static final String SPREADSHEETML_NAMESPACE = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
        private static final String TEXT_TAGNAME            = "t";

        public SharedStringsHandler(Workbook workbook)
        {
             _workbook = workbook;
             _value    = new StringBuffer();
             _index    = 0;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {
            if ((localName != null) && localName.equals(TEXT_TAGNAME) && (uri != null) && uri.equals(SPREADSHEETML_NAMESPACE))
                _value.setLength(0);
        }

        @Override
        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            if ((localName != null) && localName.equals(TEXT_TAGNAME) && (uri != null) && uri.equals(SPREADSHEETML_NAMESPACE))
            {
                String value = _value.toString();

                System.out.println("S{{{" + value + "}}}} " + _index);
                _index++;
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

        private Workbook     _workbook;
        private StringBuffer _value;
        private int          _index;
    }

    private String getColumnLabel(String cellName)
    {
        int index = 0;
        while ((index < cellName.length()) && Character.isAlphabetic(cellName.charAt(index)))
            index++;

        return cellName.substring(0, index);
    }

    private String getRowNumber(String cellName)
    {
        int index = 0;
        while ((index < cellName.length()) && Character.isAlphabetic(cellName.charAt(index)))
            index++;

        return cellName.substring(index, cellName.length());
    }

    private String getCellValue(String cellType, String cellStyle, String cellValue, StylesTable stylesTable)
    {
        DataFormatter formatter = new DataFormatter();

        if ((cellType == null) && (cellStyle != null))
        {
            try
            {
                int           styleIndex   = Integer.parseInt(cellStyle);
                XSSFCellStyle style        = stylesTable.getStyleAt(styleIndex);
                short         formatIndex  = style.getDataFormat();
                String        formatString = style.getDataFormatString();
                if (formatString == null)
                    formatString = BuiltinFormats.getBuiltinFormat(formatIndex);

                return formatter.formatRawCellContents(Double.parseDouble(cellValue.toString()), formatIndex, formatString);
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
        else if ((cellType != null) && cellType.equals("s"))
        {
            String sharedStringsTableIndex = cellValue.toString();
            try
            {
//                int                index = Integer.parseInt(sharedStringsTableIndex);
//                XSSFRichTextString rtss  = new XSSFRichTextString(_sharedStringsTable.getEntryAt(index));

//                return rtss.toString();

                return "unknown (unmaped)!";
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
        else if ((cellType != null) && cellType.equals("n"))
        {
            String sharedStringsTableIndex = cellValue.toString();
        }
        else if (cellType != null)
            logger.log(Level.WARNING, "Unsupported cell type '" + cellValue + "', '" + cellType + "', '" + cellType + "'");

        return "problem!";
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
