/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

public interface AccessibleExtendedComponent extends AccessibleComponent{
    public String getToolTipText();

    public String getTitledBorderText();

    public AccessibleKeyBinding getAccessibleKeyBinding();
}
