/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.xssf.view;

import java.util.List;
import com.arjuna.databroker.metadata.annotations.MetadataView;
import com.arjuna.databroker.metadata.annotations.GetMetadataMapping;

@MetadataView
public interface SheetView
{
    @GetMetadataMapping(name="http://rdfs.arjuna.com/xssf#hasSheetName", type="http://www.w3.org/2001/XMLSchema#string")
    public String getName();

    @GetMetadataMapping(name="http://rdfs.arjuna.com/xssf#hasColumn", type="http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq")
    public List<ColumnView> getColumns();
}