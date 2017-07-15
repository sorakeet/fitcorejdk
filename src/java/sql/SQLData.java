/**
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public interface SQLData{
    String getSQLTypeName() throws SQLException;

    void readSQL(SQLInput stream,String typeName) throws SQLException;

    void writeSQL(SQLOutput stream) throws SQLException;
}
