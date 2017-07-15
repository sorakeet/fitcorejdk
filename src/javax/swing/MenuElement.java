/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public interface MenuElement{
    public void processMouseEvent(MouseEvent event,MenuElement path[],MenuSelectionManager manager);

    public void processKeyEvent(KeyEvent event,MenuElement path[],MenuSelectionManager manager);

    public void menuSelectionChanged(boolean isIncluded);

    public MenuElement[] getSubElements();

    public Component getComponent();
}
