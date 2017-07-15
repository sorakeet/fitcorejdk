/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface WindowPeer extends ContainerPeer{
    void toFront();

    void toBack();

    void updateAlwaysOnTopState();

    void updateFocusableWindowState();

    void setModalBlocked(Dialog blocker,boolean blocked);

    void updateMinimumSize();

    void updateIconImages();

    void setOpacity(float opacity);

    void setOpaque(boolean isOpaque);

    void updateWindow();

    void repositionSecurityWarning();
}
