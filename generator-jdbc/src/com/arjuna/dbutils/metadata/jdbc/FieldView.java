/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.jdbc;

import com.arjuna.databroker.metadata.annotations.MetadataContentView;
import com.arjuna.databroker.metadata.annotations.MetadataStatementMapping;

@MetadataContentView
public interface FieldView
{
    @MetadataStatementMapping(name="http://rdfs.arjuna.com/jdbc/postgresql/database#name", type="http://www.w3.org/2001/XMLSchema#string")
    public String getName();

    @MetadataStatementMapping(name="http://rdfs.arjuna.com/jdbc/postgresql/database#hostname", type="http://www.w3.org/2001/XMLSchema#string")
    public String getHostName();

    @MetadataStatementMapping(name="http://rdfs.arjuna.com/jdbc/postgresql/database#postnumber", type="http://www.w3.org/2001/XMLSchema#string")
    public String getPostNumber();

    @MetadataStatementMapping(name="http://rdfs.arjuna.com/jdbc/postgresql/database#username", type="http://www.w3.org/2001/XMLSchema#string")
    public String getUserName();

    @MetadataStatementMapping(name="http://rdfs.arjuna.com/jdbc/postgresql/database#password", type="http://www.w3.org/2001/XMLSchema#string")
    public String getPassword();
}