/**
 * Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventListener;

public interface MenuKeyListener extends EventListener{
    void menuKeyTyped(MenuKeyEvent e);

    void menuKeyPressed(MenuKeyEvent e);

    void menuKeyReleased(MenuKeyEvent e);
}
