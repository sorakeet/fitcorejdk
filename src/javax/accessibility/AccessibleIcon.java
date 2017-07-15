/**
 * Copyright (c) 1999, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

public interface AccessibleIcon{
    public String getAccessibleIconDescription();

    public void setAccessibleIconDescription(String description);

    public int getAccessibleIconWidth();

    public int getAccessibleIconHeight();
}
