/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbutils.metadata.jdbc.generator;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;

@Startup
@Singleton
public class JDBCDatabaseMetadataScan
{
    private static final Logger logger = Logger.getLogger(JDBCDatabaseMetadataScan.class.getName());

    @PostConstruct
    public void setup()
    {
        logger.log(Level.FINE, "Setup : PostgreSQL JDBC Database Metadata Scan");

        Connection connection = null;
        try
        {
            connection = _dataSource.getConnection();

            DatabaseMetaData databaseMetaData = connection.getMetaData();

            ResultSet allTablesResultSet = databaseMetaData.getTables(null, "public", null, null);
            while (allTablesResultSet.next())
            {
//                ResultSetMetaData tableResultSetMetaData = allTablesResultSet.getMetaData();
//
//                for (int columnIndex = 1; columnIndex <= tableResultSetMetaData.getColumnCount(); columnIndex++)
//                {
//                    logger.log(Level.INFO, columnIndex +  "," + tableResultSetMetaData.getColumnName(columnIndex) + "," + tableResultSetMetaData.getColumnLabel(columnIndex) + "," + tableResultSetMetaData.getColumnClassName(columnIndex));
//                    logger.log(Level.INFO, "index = " + columnIndex);
//                    logger.log(Level.INFO, "catalogname = " + tableResultSetMetaData.getCatalogName(columnIndex));
//                    logger.log(Level.INFO, "name        = " + tableResultSetMetaData.getColumnName(columnIndex));
//                    logger.log(Level.INFO, "classname   = " + tableResultSetMetaData.getColumnClassName(columnIndex));
//                    logger.log(Level.INFO, "type        = " + tableResultSetMetaData.getColumnType(columnIndex));
//                    logger.log(Level.INFO, "typename    = " + tableResultSetMetaData.getColumnTypeName(columnIndex));
//                    logger.log(Level.INFO, "label       = " + tableResultSetMetaData.getColumnLabel(columnIndex));
//                    logger.log(Level.INFO, "displaysize = " + tableResultSetMetaData.getColumnDisplaySize(columnIndex));
//                }

                logger.log(Level.INFO, allTablesResultSet.getString("table_cat") + "," + allTablesResultSet.getString("table_schem") + "," + allTablesResultSet.getString("table_name") + "," + allTablesResultSet.getString("table_type") + "," + allTablesResultSet.getString("remarks"));
            }

            ResultSet allColumnsResultSet = databaseMetaData.getColumns(null, "public", "metadataentity", null);
            while (allColumnsResultSet.next())
            {
//                ResultSetMetaData columnResultSetMetaData = allColumnsResultSet.getMetaData();
//
//                for (int columnIndex = 1; columnIndex <= columnResultSetMetaData.getColumnCount(); columnIndex++)
//                {
//                    logger.log(Level.INFO, columnIndex +  "," + columnResultSetMetaData.getColumnName(columnIndex) + "," +  columnResultSetMetaData.getColumnLabel(columnIndex) + "," + columnResultSetMetaData.getColumnClassName(columnIndex));
//                    logger.log(Level.INFO, "index = " + columnIndex);
//                    logger.log(Level.INFO, "catalogname = " + columnResultSetMetaData.getCatalogName(columnIndex));
//                    logger.log(Level.INFO, "name        = " + columnResultSetMetaData.getColumnName(columnIndex));
//                    logger.log(Level.INFO, "classname   = " + columnResultSetMetaData.getColumnClassName(columnIndex));
//                    logger.log(Level.INFO, "type        = " + columnResultSetMetaData.getColumnType(columnIndex));
//                    logger.log(Level.INFO, "typename    = " + columnResultSetMetaData.getColumnTypeName(columnIndex));
//                    logger.log(Level.INFO, "label       = " + columnResultSetMetaData.getColumnLabel(columnIndex));
//                    logger.log(Level.INFO, "displaysize = " + columnResultSetMetaData.getColumnDisplaySize(columnIndex));
//                }

                logger.log(Level.INFO, allColumnsResultSet.getString("TABLE_CAT") + "," + allColumnsResultSet.getString("TABLE_SCHEM") + "," + allColumnsResultSet.getString("TABLE_NAME") + "," + allColumnsResultSet.getString("COLUMN_NAME") + "," + allColumnsResultSet.getString("REMARKS"));
            }

            connection.close();
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem Generating during JDBC Database Metadata Scan", throwable);

            try
            {
                if (! connection.isClosed())
                    connection.close();
            }
            catch (Throwable sqlThrowable)
            {
                logger.log(Level.WARNING, "Problem Generating during JDBC Database Metadata Scan, close", sqlThrowable);
            }
        }
    }

    @PreDestroy
    public void cleanup()
    {
        logger.log(Level.FINE, "Cleanup : PostgreSQL JDBC Database Metadata Scan");
    }

    @Resource(mappedName="java:jboss/datasources/PostgreSQLDS")
    private DataSource _dataSource;
}
