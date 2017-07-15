/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset.spi;

import javax.sql.RowSetWriter;
import java.sql.SQLException;
import java.sql.Savepoint;

public interface TransactionalWriter extends RowSetWriter{
    public void commit() throws SQLException;

    public void rollback() throws SQLException;

    public void rollback(Savepoint s) throws SQLException;
}
