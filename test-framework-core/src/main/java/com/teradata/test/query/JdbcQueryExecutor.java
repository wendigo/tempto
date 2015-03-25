/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query;

import org.slf4j.Logger;

import javax.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import static com.teradata.test.query.QueryResult.forSingleIntegerValue;
import static com.teradata.test.query.QueryResult.toSqlIndex;
import static org.slf4j.LoggerFactory.getLogger;

public class JdbcQueryExecutor
        implements QueryExecutor
{
    private static final Logger LOGGER = getLogger(JdbcQueryExecutor.class);

    private final JdbcConnectivityParamsState jdbcParamsState;
    private final JdbcConnectionsPool jdbcConnectionsPool;

    @Inject
    public JdbcQueryExecutor(JdbcConnectivityParamsState jdbcParamsState, JdbcConnectionsPool jdbcConnectionsPool)
    {
        this.jdbcParamsState = jdbcParamsState;
        this.jdbcConnectionsPool = jdbcConnectionsPool;
    }

    @Override
    public QueryResult executeQuery(String sql, QueryParam[] params)
    {
        LOGGER.debug("executing query {} with params {}", sql, params);

        try {
            if (params.length == 0) {
                return executeQueryNoParams(sql);
            }
            else {
                return executeQueryWithParams(sql, params);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Error while executing query: " + sql + ", params: " + Arrays.toString(params), e);
        }
    }

    private QueryResult executeQueryNoParams(String sql)
            throws SQLException
    {
        try (
                Connection connection = jdbcConnectionsPool.connectionFor(jdbcParamsState);
                Statement statement = connection.createStatement()
        ) {
            if (isSelect(sql)) {
                ResultSet rs = statement.executeQuery(sql);
                return buildQueryResult(rs);
            }
            else {
                return forSingleIntegerValue(statement.executeUpdate(sql));
            }
        }
    }

    // TODO - remove this method as soon as Presto supports prepared statements
    private QueryResult executeQueryWithParams(String sql, QueryParam[] params)
            throws SQLException
    {
        try (
                Connection connection = jdbcConnectionsPool.connectionFor(jdbcParamsState);
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            setQueryParams(statement, params);

            if (isSelect(sql)) {
                ResultSet rs = statement.executeQuery();
                return buildQueryResult(rs);
            }
            else {
                return forSingleIntegerValue(statement.executeUpdate());
            }
        }
    }

    private QueryResult buildQueryResult(ResultSet rs)
            throws SQLException
    {
        return QueryResult.builder(rs.getMetaData())
                .addRows(rs)
                .build();
    }

    boolean isSelect(String sql)
    {
        return sql.trim().toLowerCase().startsWith("select");
    }

    private static void setQueryParams(PreparedStatement statement, QueryParam[] params)
            throws SQLException
    {
        for (int i = 0; i < params.length; ++i) {
            QueryParam param = params[i];
            statement.setObject(toSqlIndex(i), param.value, param.type.getVendorTypeNumber());
        }
    }
}