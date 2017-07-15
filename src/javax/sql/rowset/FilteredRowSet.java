/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset;

import java.sql.SQLException;

public interface FilteredRowSet extends WebRowSet{
    public Predicate getFilter();

    public void setFilter(Predicate p) throws SQLException;
}
