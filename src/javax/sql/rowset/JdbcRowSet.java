/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset;

import javax.sql.RowSet;
import java.sql.SQLException;
import java.sql.Savepoint;

public interface JdbcRowSet extends RowSet, Joinable{
    public boolean getShowDeleted() throws SQLException;

    public void setShowDeleted(boolean b) throws SQLException;

    public RowSetWarning getRowSetWarnings() throws SQLException;

    public void commit() throws SQLException;

    public boolean getAutoCommit() throws SQLException;

    public void setAutoCommit(boolean autoCommit) throws SQLException;

    public void rollback() throws SQLException;

    public void rollback(Savepoint s) throws SQLException;
}
