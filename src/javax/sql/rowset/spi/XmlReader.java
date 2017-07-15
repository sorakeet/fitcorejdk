/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset.spi;

import javax.sql.RowSetReader;
import javax.sql.rowset.WebRowSet;
import java.io.Reader;
import java.sql.SQLException;

public interface XmlReader extends RowSetReader{
    public void readXML(WebRowSet caller,Reader reader)
            throws SQLException;
}
