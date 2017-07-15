/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

import java.io.InputStream;

public interface Blob{
    long length() throws SQLException;

    byte[] getBytes(long pos,int length) throws SQLException;

    InputStream getBinaryStream() throws SQLException;

    long position(byte pattern[],long start) throws SQLException;

    long position(Blob pattern,long start) throws SQLException;
    // -------------------------- JDBC 3.0 -----------------------------------

    int setBytes(long pos,byte[] bytes) throws SQLException;

    int setBytes(long pos,byte[] bytes,int offset,int len) throws SQLException;

    java.io.OutputStream setBinaryStream(long pos) throws SQLException;

    void truncate(long len) throws SQLException;

    void free() throws SQLException;

    InputStream getBinaryStream(long pos,long length) throws SQLException;
}
