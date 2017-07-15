/**
 * Copyright (c) 1995, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface MenuPeer extends MenuItemPeer{
    void addSeparator();

    void addItem(MenuItem item);

    void delItem(int index);
}
