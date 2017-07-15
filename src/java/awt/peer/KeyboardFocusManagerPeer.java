/**
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface KeyboardFocusManagerPeer{
    Window getCurrentFocusedWindow();

    void setCurrentFocusedWindow(Window win);

    Component getCurrentFocusOwner();

    void setCurrentFocusOwner(Component comp);

    void clearGlobalFocusOwner(Window activeWindow);
}
