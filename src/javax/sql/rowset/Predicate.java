/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset;

import javax.sql.RowSet;
import java.sql.SQLException;

// <h3>3.0 FilteredRowSet Internals</h3>
// internalNext, Frist, Last. Discuss guidelines on how to approach this
// and cite examples in reference implementations.
public interface Predicate{
    public boolean evaluate(RowSet rs);

    public boolean evaluate(Object value,int column) throws SQLException;

    public boolean evaluate(Object value,String columnName) throws SQLException;
}
