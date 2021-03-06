/**
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

public interface AccessibleExtendedTable extends AccessibleTable{
    public int getAccessibleRow(int index);

    public int getAccessibleColumn(int index);

    public int getAccessibleIndex(int r,int c);
}
