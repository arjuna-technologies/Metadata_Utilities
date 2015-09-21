/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.csv.view;

import com.arjuna.databroker.metadata.annotations.MetadataView;
import com.arjuna.databroker.metadata.annotations.GetMetadataMapping;

@MetadataView
public interface ColumnView
{
    @GetMetadataMapping(name="http://rdfs.arjuna.com/csv#hasColumnName", type="http://www.w3.org/2001/XMLSchema#string")
    public String getName();

    @GetMetadataMapping(name="http://rdfs.arjuna.com/csv#hasColumnType", type="http://www.w3.org/2001/XMLSchema#string")
    public String getType();
}