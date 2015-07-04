/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.tests.json.view;

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
import com.arjuna.dbutils.metadata.json.view.FieldView;
import com.arjuna.dbutils.metadata.json.view.ArrayView;

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

            String exampleJSON01 = Utils.loadInputStream(FieldViewTest.class.getResourceAsStream("ExampleJSON01.rdf"));

            ids.add("exampleJSON01");
            contentMap.put("exampleJSON01", exampleJSON01);

            DummyMetadataContentStore dummyMetadataContentStore = new DummyMetadataContentStore(ids, contentMap, descriptionIdMap, parentIdMap, childrenIdsMap);
            MetadataInventory         metadataInventory         = new StoreMetadataInventory(dummyMetadataContentStore);
            Metadata                  metadata                  = metadataInventory.metadata("exampleJSON01").getMetadata();

            _metadataContent = metadata.contents().selector(RDFMetadataContentsSelector.class).withPath("http://rdf.example.org/JSON_Test#2fbcd7b8-f2d7-4894-b1c4-e2a5e70e9bfa").getMetadataContent();
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

        ArrayView arrayView = _metadataContent.getView(ArrayView.class);
        assertNotNull("Not expecting null ArrayView object", arrayView);

        List<FieldView> fieldsValue = arrayView.getFields();
        assertNotNull("Not expecting null FieldView list value", fieldsValue);
        assertEquals("Unexpecting length of FieldView list value", 2, fieldsValue.size());

        FieldView fieldViewValue0 = fieldsValue.get(0);
        assertNotNull("Not expecting null FieldView[0] value", fieldViewValue0);
        assertEquals("Unexpecting value for FieldView[0].name", "name01", fieldViewValue0.getName());
        assertEquals("Unexpecting value for FieldView[0].type", "type01", fieldViewValue0.getType());

        FieldView fieldViewValue1 = fieldsValue.get(1);
        assertNotNull("Not expecting null FieldView[1] value", fieldViewValue1);
        assertEquals("Unexpecting value for FieldView[1].name", "name02", fieldViewValue1.getName());
        assertEquals("Unexpecting value for FieldView[1].type", "type02", fieldViewValue1.getType());
    }

    private static MetadataContent _metadataContent;
}
