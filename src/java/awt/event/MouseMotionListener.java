/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.util.EventListener;

public interface MouseMotionListener extends EventListener{
    public void mouseDragged(MouseEvent e);

    public void mouseMoved(MouseEvent e);
}
