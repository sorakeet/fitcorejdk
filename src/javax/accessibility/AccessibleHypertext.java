/**
 * Copyright (c) 1998, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

public interface AccessibleHypertext extends AccessibleText{
    public abstract int getLinkCount();

    public abstract AccessibleHyperlink getLink(int linkIndex);

    public abstract int getLinkIndex(int charIndex);
}
