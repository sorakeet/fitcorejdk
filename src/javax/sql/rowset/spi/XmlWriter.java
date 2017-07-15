/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset.spi;

import javax.sql.RowSetWriter;
import javax.sql.rowset.WebRowSet;
import java.io.Writer;
import java.sql.SQLException;

public interface XmlWriter extends RowSetWriter{
    public void writeXML(WebRowSet caller,Writer writer)
            throws SQLException;
}
