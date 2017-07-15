/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

import java.io.Reader;

public interface Clob{
    long length() throws SQLException;

    String getSubString(long pos,int length) throws SQLException;

    Reader getCharacterStream() throws SQLException;

    java.io.InputStream getAsciiStream() throws SQLException;

    long position(String searchstr,long start) throws SQLException;

    long position(Clob searchstr,long start) throws SQLException;
    //---------------------------- jdbc 3.0 -----------------------------------

    int setString(long pos,String str) throws SQLException;

    int setString(long pos,String str,int offset,int len) throws SQLException;

    java.io.OutputStream setAsciiStream(long pos) throws SQLException;

    java.io.Writer setCharacterStream(long pos) throws SQLException;

    void truncate(long len) throws SQLException;

    void free() throws SQLException;

    Reader getCharacterStream(long pos,long length) throws SQLException;
}
