/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public interface Savepoint{
    int getSavepointId() throws SQLException;

    String getSavepointName() throws SQLException;
}
