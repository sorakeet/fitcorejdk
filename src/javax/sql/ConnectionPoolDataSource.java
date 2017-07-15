/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql;

import java.sql.SQLException;

public interface ConnectionPoolDataSource extends CommonDataSource{
    PooledConnection getPooledConnection() throws SQLException;

    PooledConnection getPooledConnection(String user,String password)
            throws SQLException;
}
