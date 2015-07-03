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

            _metadataContent = metadata.contents().selector(RDFMetadataContentsSelector.class).withPath("http://rdf.example.org/JSON_Test#TimeSheet").getMetadataContent();
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
        assertNotNull("Not expecting null Spreadsheet View object", arrayView);

        List<FieldView> fieldsValue = arrayView.getFields();
        assertNotNull("Not expecting null SheetView list value", fieldsValue);
        assertEquals("Unexpecting length of SheetView list value", 2, fieldsValue.size());

        FieldView fieldViewValue = fieldsValue.get(0);
        assertNotNull("Not expecting null FieldView[0] value", fieldViewValue);
        assertEquals("Unexpecting value for FieldView[0].name", "name", fieldViewValue.getName());
        assertEquals("Unexpecting value for FieldView[0].type", "string", fieldViewValue.getType());
    }

    private static MetadataContent _metadataContent;
}