/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql;

import java.sql.SQLException;

public interface XAConnection extends PooledConnection{
    javax.transaction.xa.XAResource getXAResource() throws SQLException;
}
