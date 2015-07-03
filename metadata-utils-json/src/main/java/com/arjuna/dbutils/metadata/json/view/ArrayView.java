/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.json.view;

import java.util.List;
import com.arjuna.databroker.metadata.annotations.MetadataView;
import com.arjuna.databroker.metadata.annotations.GetMetadataMapping;

@MetadataView
public interface ArrayView
{
    @GetMetadataMapping(name="http://rdfs.arjuna.com/json#hasField", type="http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq")
    public List<FieldView> getFields();
}