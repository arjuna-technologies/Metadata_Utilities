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

            String exampleDB01 = Utils.loadInputStream(TableViewTest.class.getResourceAsStream("ExampleDB01.rdf"));

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

        TableView tableView0Value = tableViewsValue.get(0);
        assertNotNull("Not expecting null TableView[0] value", tableView0Value);
        assertEquals("Unexpecting value for TableView[0].name", "accesscontrolentity", tableView0Value.getName());
        List<FieldView> fieldViews0Value = tableView0Value.getFields();
        assertNotNull("Not expecting null FieldView list 0 value", fieldViews0Value);
        assertEquals("Unexpecting length of TableView list 0 value", 10, fieldViews0Value.size());

        TableView tableView1Value = tableViewsValue.get(1);
        assertNotNull("Not expecting null TableView[1] value", tableView1Value);
        assertEquals("Unexpecting value for TableView[1].name", "databrokerentity", tableView1Value.getName());
        List<FieldView> fieldViews1Value = tableView1Value.getFields();
        assertNotNull("Not expecting null FieldView list 1 value", fieldViews1Value);
        assertEquals("Unexpecting length of TableView list 1 value", 5, fieldViews1Value.size());

        TableView tableView2Value = tableViewsValue.get(2);
        assertNotNull("Not expecting null TableView[2] value", tableView2Value);
        assertEquals("Unexpecting value for TableView[2].name", "dbwp_roles", tableView2Value.getName());
        List<FieldView> fieldViews2Value = tableView2Value.getFields();
        assertNotNull("Not expecting null FieldView list 2 value", fieldViews2Value);
        assertEquals("Unexpecting length of TableView list 2 value", 4, fieldViews2Value.size());

        TableView tableView3Value = tableViewsValue.get(3);
        assertNotNull("Not expecting null TableView[3] value", tableView3Value);
        assertEquals("Unexpecting value for TableView[3].name", "dbwp_users", tableView3Value.getName());
        List<FieldView> fieldViews3Value = tableView3Value.getFields();
        assertNotNull("Not expecting null FieldView list 3 value", fieldViews3Value);
        assertEquals("Unexpecting length of TableView list 3 value", 2, fieldViews3Value.size());

        TableView tableView4Value = tableViewsValue.get(4);
        assertNotNull("Not expecting null TableView[4] value", tableView4Value);
        assertEquals("Unexpecting value for TableView[4].name", "metadataentity", tableView4Value.getName());
        List<FieldView> fieldViews4Value = tableView4Value.getFields();
        assertNotNull("Not expecting null FieldView list 4 value", fieldViews4Value);
        assertEquals("Unexpecting length of TableView list 4 value", 4, fieldViews4Value.size());
    }

    private static MetadataContent _metadataContent;
}
