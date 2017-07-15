/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public interface Struct{
    String getSQLTypeName() throws SQLException;

    Object[] getAttributes() throws SQLException;

    Object[] getAttributes(java.util.Map<String,Class<?>> map)
            throws SQLException;
}
