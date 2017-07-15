/**
 * Copyright (c) 1995, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface MenuBarPeer extends MenuComponentPeer{
    void addMenu(Menu m);

    void delMenu(int index);

    void addHelpMenu(Menu m);
}
