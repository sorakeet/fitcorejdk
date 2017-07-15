/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface PooledConnection{
    Connection getConnection() throws SQLException;

    void close() throws SQLException;

    void addConnectionEventListener(ConnectionEventListener listener);

    void removeConnectionEventListener(ConnectionEventListener listener);

    public void addStatementEventListener(StatementEventListener listener);

    public void removeStatementEventListener(StatementEventListener listener);
}
