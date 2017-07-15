/**
 * Copyright (c) 1995, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

public interface ScrollbarPeer extends ComponentPeer{
    void setValues(int value,int visible,int minimum,int maximum);

    void setLineIncrement(int l);

    void setPageIncrement(int l);
}
