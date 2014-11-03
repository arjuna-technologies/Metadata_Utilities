/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.xssf;

import java.util.List;

import com.arjuna.databroker.metadata.annotations.MetadataView;
import com.arjuna.databroker.metadata.annotations.GetMetadataMapping;

@MetadataView
public interface SpreadSheetView
{
	@GetMetadataMapping(name="http://rdfs.arjuna.com/xssf#hasSpreadSheetName", type="http://www.w3.org/2001/XMLSchema#string")
    public String getName();

	@GetMetadataMapping(name="http://rdfs.arjuna.com/xssf#hasSheet", type="http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq")
    public List<SheetView> getSheets();
}