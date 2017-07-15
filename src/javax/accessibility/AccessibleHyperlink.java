/**
 * Copyright (c) 1998, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

public abstract class AccessibleHyperlink implements AccessibleAction{
    public abstract boolean isValid();

    public abstract int getAccessibleActionCount();

    public abstract String getAccessibleActionDescription(int i);

    public abstract boolean doAccessibleAction(int i);

    public abstract Object getAccessibleActionObject(int i);

    public abstract Object getAccessibleActionAnchor(int i);

    public abstract int getStartIndex();

    public abstract int getEndIndex();
}
