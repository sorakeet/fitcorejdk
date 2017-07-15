/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface WebRowSet extends CachedRowSet{
    public static String PUBLIC_XML_SCHEMA=
            "--//Oracle Corporation//XSD Schema//EN";
    public static String SCHEMA_SYSTEM_ID="http://java.sun.com/xml/ns/jdbc/webrowset.xsd";

    public void readXml(Reader reader) throws SQLException;

    public void readXml(InputStream iStream) throws SQLException, IOException;

    public void writeXml(ResultSet rs,Writer writer) throws SQLException;

    public void writeXml(ResultSet rs,OutputStream oStream) throws SQLException, IOException;

    public void writeXml(Writer writer) throws SQLException;

    public void writeXml(OutputStream oStream) throws SQLException, IOException;
}
