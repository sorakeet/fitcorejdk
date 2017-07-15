/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventListener;

public interface MenuListener extends EventListener{
    void menuSelected(MenuEvent e);

    void menuDeselected(MenuEvent e);

    void menuCanceled(MenuEvent e);
}
