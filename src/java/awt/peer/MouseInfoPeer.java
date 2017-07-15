/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface MouseInfoPeer{
    int fillPointWithCoords(Point point);

    boolean isWindowUnderMouse(Window w);
}
