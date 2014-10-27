/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.jdbc;

import java.util.List;
import com.arjuna.databroker.metadata.annotations.MetadataContentView;
import com.arjuna.databroker.metadata.annotations.MetadataStatementMapping;

@MetadataContentView
public interface TableView
{
    @MetadataStatementMapping(name="http://rdfs.arjuna.com/jdbc/postgresql/table#name", type="http://www.w3.org/2001/XMLSchema#string")
    public String getName();

    @MetadataStatementMapping(name="http://rdfs.arjuna.com/jdbc/postgresql/table#fields", type="http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq")
    public List<FieldView> getFields();
}