/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface FramePeer extends WindowPeer{
    void setTitle(String title);

    void setMenuBar(MenuBar mb);

    void setResizable(boolean resizeable);

    int getState();

    void setState(int state);

    void setMaximizedBounds(Rectangle bounds);

    // TODO: This is only used in EmbeddedFrame, and should probably be moved
    // into an EmbeddedFramePeer which would extend FramePeer
    void setBoundsPrivate(int x,int y,int width,int height);

    // TODO: This is only used in EmbeddedFrame, and should probably be moved
    // into an EmbeddedFramePeer which would extend FramePeer
    Rectangle getBoundsPrivate();

    void emulateActivation(boolean activate);
}
