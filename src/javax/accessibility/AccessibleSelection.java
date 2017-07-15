/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

public interface AccessibleSelection{
    public int getAccessibleSelectionCount();

    public Accessible getAccessibleSelection(int i);

    public boolean isAccessibleChildSelected(int i);

    public void addAccessibleSelection(int i);

    public void removeAccessibleSelection(int i);

    public void clearAccessibleSelection();

    public void selectAllAccessibleSelection();
}
