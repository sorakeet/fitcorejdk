/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql;

import java.sql.SQLException;

public interface RowSetReader{
    void readData(RowSetInternal caller) throws SQLException;
}
