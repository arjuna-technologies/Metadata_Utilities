/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.jdbc;

import com.arjuna.databroker.metadata.annotations.MetadataView;
import com.arjuna.databroker.metadata.annotations.GetMetadataMapping;

@MetadataView
public interface FieldView
{
    @GetMetadataMapping(name="http://rdfs.arjuna.com/jdbc/postgresql#hasFieldName", type="http://www.w3.org/2001/XMLSchema#string")
    public String getName();

    @GetMetadataMapping(name="http://rdfs.arjuna.com/jdbc/postgresql#hasFieldType", type="http://www.w3.org/2001/XMLSchema#string")
    public String getType();
}