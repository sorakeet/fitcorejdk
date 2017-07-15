/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventListener;

public interface PopupMenuListener extends EventListener{
    void popupMenuWillBecomeVisible(PopupMenuEvent e);

    void popupMenuWillBecomeInvisible(PopupMenuEvent e);

    void popupMenuCanceled(PopupMenuEvent e);
}
