/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.csv.view;

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
import com.arjuna.dbutils.metadata.csv.view.ColumnView;
import com.arjuna.dbutils.metadata.csv.view.TableView;

public class TableViewTest
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

            String exampleCSV01 = Utils.loadInputStream(ColumnViewTest.class.getResourceAsStream("ExampleCSV01.rdf"));

            ids.add("exampleCSV01");
            contentMap.put("exampleCSV01", exampleCSV01);

            DummyMetadataContentStore dummyMetadataContentStore = new DummyMetadataContentStore(ids, contentMap, descriptionIdMap, parentIdMap, childrenIdsMap);
            MetadataInventory         metadataInventory         = new StoreMetadataInventory(dummyMetadataContentStore);
            Metadata                  metadata                  = metadataInventory.metadata("exampleCSV01").getMetadata();

            _metadataContent = metadata.contents().selector(RDFMetadataContentsSelector.class).withPath("http://rdf.example.org/CSV_Test#2fbcd7b8-f2d7-4894-b1c4-e2a5e70e9bfa").getMetadataContent();
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

        TableView tableView = _metadataContent.getView(TableView.class);
        assertNotNull("Not expecting null Table View object", tableView);

        List<ColumnView> columnsValue = tableView.getColumns();
        assertNotNull("Not expecting null ColumnView list value", columnsValue);
        assertEquals("Unexpecting length of ColumnView list value", 2, columnsValue.size());
    }

    private static MetadataContent _metadataContent;
}
