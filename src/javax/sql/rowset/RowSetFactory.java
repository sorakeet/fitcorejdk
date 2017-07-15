/**
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset;

import java.sql.SQLException;

public interface RowSetFactory{
    public CachedRowSet createCachedRowSet() throws SQLException;

    public FilteredRowSet createFilteredRowSet() throws SQLException;

    public JdbcRowSet createJdbcRowSet() throws SQLException;

    public JoinRowSet createJoinRowSet() throws SQLException;

    public WebRowSet createWebRowSet() throws SQLException;
}
