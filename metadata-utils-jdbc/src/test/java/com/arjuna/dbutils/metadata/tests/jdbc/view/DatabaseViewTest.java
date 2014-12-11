/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.jdbc.view;

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
import com.arjuna.dbutils.metadata.jdbc.view.DatabaseView;
import com.arjuna.dbutils.metadata.jdbc.view.TableView;

public class DatabaseViewTest
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

            String exampleDB01 = Utils.loadInputStream(DatabaseViewTest.class.getResourceAsStream("ExampleDB01.rdf"));

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
    public void databaseView()
    {
        assertNotNull("Not expecting null Metadata Content object", _metadataContent);

        DatabaseView databaseView = _metadataContent.getView(DatabaseView.class);
        assertNotNull("Not expecting null Database View object", databaseView);

        String typeValue = databaseView.getType();
        assertEquals("Unexpecting type value", "PostgreSQL", typeValue);

        String hostnameValue = databaseView.getHostName();
        assertEquals("Unexpecting hostname value", "localhost", hostnameValue);

        String portNumberValue = databaseView.getPortNumber();
        assertEquals("Unexpecting port number value", "0", portNumberValue);

        String userNameValue = databaseView.getUserName();
        assertEquals("Unexpecting user name value", "test_user", userNameValue);

        String passwordValue = databaseView.getPassword();
        assertEquals("Unexpecting password value", "test_password", passwordValue);

        String nameValue = databaseView.getName();
        assertEquals("Unexpecting name value", "databroker", nameValue);

        List<TableView> tableViewsValue = databaseView.getTables();
        assertNotNull("Not expecting null TableView list value", tableViewsValue);
        assertEquals("Unexpecting length of TableView list value", 5, tableViewsValue.size());
    }

    private static MetadataContent _metadataContent;
}
