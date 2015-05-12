/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.xssf.view;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.arjuna.databroker.metadata.Metadata;
import com.arjuna.databroker.metadata.MetadataContent;
import com.arjuna.databroker.metadata.MetadataInventory;
import com.arjuna.databroker.metadata.rdf.StoreMetadataInventory;
import com.arjuna.databroker.metadata.rdf.selectors.RDFMetadataContentsSelector;
import com.arjuna.dbutils.metadata.xssf.view.ColumnView;
import com.arjuna.dbutils.metadata.xssf.view.SheetView;
import com.arjuna.dbutils.metadata.xssf.view.WorkbookView;

public class ColumnViewTest
{
    @BeforeClass
    public static void setupInventory()
    {
        try
        {
            List<String>              ids              = new LinkedList<String>();
            Map<String, String>       contentMap       = new HashMap<String, String>();
            Map<String, String>       descriptionIdMap = new HashMap<String, String>();
            Map<String, String>       parentIdMap      = new HashMap<String, String>();
            Map<String, List<String>> childrenIdsMap   = new HashMap<String, List<String>>();

            String exampleXSSF01 = Utils.loadInputStream(ColumnViewTest.class.getResourceAsStream("ExampleXSSF01.rdf"));

            ids.add("exampleXSSF01");
            contentMap.put("exampleXSSF01", exampleXSSF01);

            DummyMetadataContentStore dummyMetadataContentStore = new DummyMetadataContentStore(ids, contentMap, descriptionIdMap, parentIdMap, childrenIdsMap);
            MetadataInventory         metadataInventory         = new StoreMetadataInventory(dummyMetadataContentStore);
            Metadata                  metadata                  = metadataInventory.metadata("exampleXSSF01").getMetadata();

            _metadataContent = metadata.contents().selector(RDFMetadataContentsSelector.class).withPath("http://rdf.example.org/XSSF_Test#TimeSheet").getMetadataContent();
        }
        catch (Throwable throwable)
        {
            fail("Failed to populate Metadata Inventory");
        }
    }

    @Test
    public void columnView()
    {
        assertNotNull("Not expecting null Metadata Content object", _metadataContent);

        WorkbookView spreadsheetView = _metadataContent.getView(WorkbookView.class);
        assertNotNull("Not expecting null Spreadsheet View object", spreadsheetView);

        List<SheetView> sheetViewsValue = spreadsheetView.getSheets();
        assertNotNull("Not expecting null SheetView list value", sheetViewsValue);
        assertEquals("Unexpecting length of SheetView list value", 1, sheetViewsValue.size());

        SheetView sheetViewValue = sheetViewsValue.get(0);
        assertNotNull("Not expecting null SheetView[0] value", sheetViewValue);

        List<ColumnView> columnViewsValue = sheetViewValue.getColumns();
        assertNotNull("Not expecting null ColumnView list value", columnViewsValue);
        assertEquals("Unexpecting length of ColumnView list value", 2, columnViewsValue.size());

        ColumnView columnView0Value = columnViewsValue.get(0);
        assertNotNull("Not expecting null ColumnView 0 value", columnView0Value);
        assertEquals("Unexpecting value for ColumnView[0].name", "task", columnView0Value.getName());
        assertEquals("Unexpecting value for ColumnView[0].number", "0", columnView0Value.getNumber());

        ColumnView columnView1Value = columnViewsValue.get(1);
        assertNotNull("Not expecting null ColumnView 1 value", columnView1Value);
        assertEquals("Unexpecting value for ColumnView[1].name", "time", columnView1Value.getName());
        assertEquals("Unexpecting value for ColumnView[1].number", "1", columnView1Value.getNumber());
    }

    private static MetadataContent _metadataContent;
}
