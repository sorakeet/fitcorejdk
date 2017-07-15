/**
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd;

import java.util.EventListener;

public interface DragSourceMotionListener extends EventListener{
    void dragMouseMoved(DragSourceDragEvent dsde);
}
