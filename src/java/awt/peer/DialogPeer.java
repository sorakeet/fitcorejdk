/**
 * Copyright (c) 1995, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface DialogPeer extends WindowPeer{
    void setTitle(String title);

    void setResizable(boolean resizeable);

    void blockWindows(java.util.List<Window> windows);
}
