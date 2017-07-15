/**
 * Copyright (c) 1996, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface ScrollPanePeer extends ContainerPeer{
    int getHScrollbarHeight();

    int getVScrollbarWidth();

    void setScrollPosition(int x,int y);

    void childResized(int w,int h);

    void setUnitIncrement(Adjustable adj,int u);

    void setValue(Adjustable adj,int v);
}
