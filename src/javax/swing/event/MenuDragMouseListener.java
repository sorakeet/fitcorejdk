/**
 * Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventListener;

public interface MenuDragMouseListener extends EventListener{
    void menuDragMouseEntered(MenuDragMouseEvent e);

    void menuDragMouseExited(MenuDragMouseEvent e);

    void menuDragMouseDragged(MenuDragMouseEvent e);

    void menuDragMouseReleased(MenuDragMouseEvent e);
}
