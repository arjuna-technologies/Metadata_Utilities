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
import com.arjuna.dbutils.metadata.xssf.view.SheetView;
import com.arjuna.dbutils.metadata.xssf.view.WorkbookView;

public class WorkbookViewTest
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

            String exampleXSSF01 = Utils.loadInputStream(WorkbookViewTest.class.getResourceAsStream("ExampleXSSF01.rdf"));

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
    public void spreadsheetView()
    {
        assertNotNull("Not expecting null Metadata Content object", _metadataContent);

        WorkbookView workbookView = _metadataContent.getView(WorkbookView.class);
        assertNotNull("Not expecting null Workbook View object", workbookView);

        String nameValue = workbookView.getName();
        assertEquals("Unexpecting name value", "TimeSheet", nameValue);

        List<SheetView> sheetViewsValue = workbookView.getSheets();
        assertNotNull("Not expecting null SheetView list value", sheetViewsValue);
        assertEquals("Unexpecting length of SheetView list value", 1, sheetViewsValue.size());
    }

    private static MetadataContent _metadataContent;
}
