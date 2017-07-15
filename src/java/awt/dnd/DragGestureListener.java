/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd;

import java.util.EventListener;

public interface DragGestureListener extends EventListener{
    void dragGestureRecognized(DragGestureEvent dge);
}
