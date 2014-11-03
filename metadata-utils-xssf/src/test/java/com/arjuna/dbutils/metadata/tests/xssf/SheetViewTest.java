/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.xssf;

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
import com.arjuna.dbutils.metadata.xssf.SpreadSheetView;
import com.arjuna.dbutils.metadata.xssf.ColumnView;
import com.arjuna.dbutils.metadata.xssf.SheetView;

public class SheetViewTest
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

            _metadataContent = metadata.contents().selector(RDFMetadataContentsSelector.class).withPath("http://rdf.example.org/PS_Test#databroker").getMetadataContent();
        }
        catch (Throwable throwable)
        {
            fail("Failed to populate Metadata Inventory");
        }
    }

    @Test
    public void tableView()
    {
        assertNotNull("Not expecting null Metadata Content object", _metadataContent);

        SpreadSheetView spreadSheetView = _metadataContent.getView(SpreadSheetView.class);
        assertNotNull("Not expecting null SpreadSheet View object", spreadSheetView);

        List<SheetView> sheetViewsValue = spreadSheetView.getSheets();
        assertNotNull("Not expecting null SheetView list value", sheetViewsValue);
        assertEquals("Unexpecting length of SheetView list value", 2, sheetViewsValue.size());

        SheetView sheetView0Value = sheetViewsValue.get(0);
        assertNotNull("Not expecting null SheetView[0] value", sheetView0Value);
        assertEquals("Unexpecting value for SheetView[0].name", "accesscontrolentity", sheetView0Value.getName());
        List<ColumnView> columnViews0Value = sheetView0Value.getColumns();
        assertNotNull("Not expecting null ColumnView list 0 value", columnViews0Value);
        assertEquals("Unexpecting length of ColumnView list 0 value", 10, columnViews0Value.size());

        SheetView sheetView1Value = sheetViewsValue.get(1);
        assertNotNull("Not expecting null SheetView[1] value", sheetView1Value);
        assertEquals("Unexpecting value for SheetView[1].name", "databrokerentity", sheetView1Value.getName());
        List<ColumnView> columnViews1Value = sheetView1Value.getColumns();
        assertNotNull("Not expecting null ColumnView list 1 value", columnViews1Value);
        assertEquals("Unexpecting length of ColumnView list 1 value", 5, columnViews1Value.size());
    }

    private static MetadataContent _metadataContent;
}