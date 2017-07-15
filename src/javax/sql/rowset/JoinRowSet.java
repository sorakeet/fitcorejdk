/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset;

import javax.sql.RowSet;
import java.sql.SQLException;
import java.util.Collection;

public interface JoinRowSet extends WebRowSet{
    public static int CROSS_JOIN=0;
    public static int INNER_JOIN=1;
    public static int LEFT_OUTER_JOIN=2;
    public static int RIGHT_OUTER_JOIN=3;
    public static int FULL_JOIN=4;

    public void addRowSet(Joinable rowset) throws SQLException;

    public void addRowSet(RowSet rowset,int columnIdx) throws SQLException;

    public void addRowSet(RowSet rowset,
                          String columnName) throws SQLException;

    public void addRowSet(RowSet[] rowset,
                          int[] columnIdx) throws SQLException;

    public void addRowSet(RowSet[] rowset,
                          String[] columnName) throws SQLException;

    public Collection<?> getRowSets() throws SQLException;

    public String[] getRowSetNames() throws SQLException;

    public CachedRowSet toCachedRowSet() throws SQLException;

    public boolean supportsCrossJoin();

    public boolean supportsInnerJoin();

    public boolean supportsLeftOuterJoin();

    public boolean supportsRightOuterJoin();

    public boolean supportsFullJoin();

    public String getWhereClause() throws SQLException;

    public int getJoinType() throws SQLException;

    public void setJoinType(int joinType) throws SQLException;
}
