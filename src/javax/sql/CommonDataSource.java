/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public interface CommonDataSource{
    PrintWriter getLogWriter() throws SQLException;

    void setLogWriter(PrintWriter out) throws SQLException;

    int getLoginTimeout() throws SQLException;

    void setLoginTimeout(int seconds) throws SQLException;
    //------------------------- JDBC 4.1 -----------------------------------

    public Logger getParentLogger() throws SQLFeatureNotSupportedException;
}
