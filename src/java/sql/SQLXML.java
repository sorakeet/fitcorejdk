/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public interface SQLXML{
    void free() throws SQLException;

    InputStream getBinaryStream() throws SQLException;

    OutputStream setBinaryStream() throws SQLException;

    Reader getCharacterStream() throws SQLException;

    Writer setCharacterStream() throws SQLException;

    String getString() throws SQLException;

    void setString(String value) throws SQLException;

    <T extends Source> T getSource(Class<T> sourceClass) throws SQLException;

    <T extends Result> T setResult(Class<T> resultClass) throws SQLException;
}
