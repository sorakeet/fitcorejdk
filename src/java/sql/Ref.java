/**
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public interface Ref{
    String getBaseTypeName() throws SQLException;

    Object getObject(java.util.Map<String,Class<?>> map) throws SQLException;

    Object getObject() throws SQLException;

    void setObject(Object value) throws SQLException;
}
