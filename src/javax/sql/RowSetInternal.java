/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowSetInternal{
    Object[] getParams() throws SQLException;

    Connection getConnection() throws SQLException;

    void setMetaData(RowSetMetaData md) throws SQLException;

    public ResultSet getOriginal() throws SQLException;

    public ResultSet getOriginalRow() throws SQLException;
}
