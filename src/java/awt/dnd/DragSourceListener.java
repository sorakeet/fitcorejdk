/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd;

import java.util.EventListener;

public interface DragSourceListener extends EventListener{
    void dragEnter(DragSourceDragEvent dsde);

    void dragOver(DragSourceDragEvent dsde);

    void dropActionChanged(DragSourceDragEvent dsde);

    void dragExit(DragSourceEvent dse);

    void dragDropEnd(DragSourceDropEvent dsde);
}
