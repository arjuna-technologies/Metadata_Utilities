/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.jdbc.view;

import java.util.List;

import com.arjuna.databroker.metadata.annotations.MetadataView;
import com.arjuna.databroker.metadata.annotations.GetMetadataMapping;

@MetadataView
public interface DatabaseView
{
	@GetMetadataMapping(name="http://rdfs.arjuna.com/jdbc/postgresql#hasDatabaseType", type="http://www.w3.org/2001/XMLSchema#string")
    public String getType();

	@GetMetadataMapping(name="http://rdfs.arjuna.com/jdbc/postgresql#hasDatabaseHostName", type="http://www.w3.org/2001/XMLSchema#string")
    public String getHostName();

	@GetMetadataMapping(name="http://rdfs.arjuna.com/jdbc/postgresql#hasDatabasePortNumber", type="http://www.w3.org/2001/XMLSchema#string")
    public String getPortNumber();

	@GetMetadataMapping(name="http://rdfs.arjuna.com/jdbc/postgresql#hasDatabaseUserName", type="http://www.w3.org/2001/XMLSchema#string")
    public String getUserName();

	@GetMetadataMapping(name="http://rdfs.arjuna.com/jdbc/postgresql#hasDatabasePassword", type="http://www.w3.org/2001/XMLSchema#string")
    public String getPassword();

	@GetMetadataMapping(name="http://rdfs.arjuna.com/jdbc/postgresql#hasDatabaseName", type="http://www.w3.org/2001/XMLSchema#string")
    public String getName();

	@GetMetadataMapping(name="http://rdfs.arjuna.com/jdbc/postgresql#hasDatabaseTable", type="http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq")
    public List<TableView> getTables();
}