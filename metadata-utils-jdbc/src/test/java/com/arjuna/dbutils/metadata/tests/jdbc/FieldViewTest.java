/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.jdbc;

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
import com.arjuna.dbutils.metadata.jdbc.DatabaseView;
import com.arjuna.dbutils.metadata.jdbc.FieldView;
import com.arjuna.dbutils.metadata.jdbc.TableView;

public class FieldViewTest
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

            String exampleDB01 = Utils.loadInputStream(FieldViewTest.class.getResourceAsStream("ExampleDB01.rdf"));

            ids.add("exampleDB01");
            contentMap.put("exampleDB01", exampleDB01);

            DummyMetadataContentStore dummyMetadataContentStore = new DummyMetadataContentStore(ids, contentMap, descriptionIdMap, parentIdMap, childrenIdsMap);
            MetadataInventory         metadataInventory         = new StoreMetadataInventory(dummyMetadataContentStore);
            Metadata                  metadata                  = metadataInventory.metadata("exampleDB01").getMetadata();

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

        DatabaseView databaseView = _metadataContent.getView(DatabaseView.class);
        assertNotNull("Not expecting null Database View object", databaseView);

        List<TableView> tableViewsValue = databaseView.getTables();
        assertNotNull("Not expecting null TableView list value", tableViewsValue);
        assertEquals("Unexpecting length of TableView list value", 5, tableViewsValue.size());

        TableView tableViewValue = tableViewsValue.get(3);
        assertNotNull("Not expecting null TableView value", tableViewValue);

        List<FieldView> fieldViewsValue = tableViewValue.getFields();
        assertNotNull("Not expecting null FieldView list value", fieldViewsValue);
        assertEquals("Unexpecting length of FieldView list value", 2, fieldViewsValue.size());

        FieldView fieldView0Value = fieldViewsValue.get(0);
        assertNotNull("Not expecting null FieldView 0 value", fieldView0Value);
        assertEquals("Unexpecting value for FieldView[0].name", "username", fieldView0Value.getName());
        assertEquals("Unexpecting value for FieldView[0].type", "varchar", fieldView0Value.getType());

        FieldView fieldView1Value = fieldViewsValue.get(1);
        assertNotNull("Not expecting null FieldView 1 value", fieldView1Value);
        assertEquals("Unexpecting value for FieldView[1].name", "password", fieldView1Value.getName());
        assertEquals("Unexpecting value for FieldView[1].type", "varchar", fieldView1Value.getType());
    }

    private static MetadataContent _metadataContent;
}
