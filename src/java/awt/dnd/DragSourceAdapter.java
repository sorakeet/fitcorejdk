/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd;

public abstract class DragSourceAdapter
        implements DragSourceListener, DragSourceMotionListener{
    public void dragEnter(DragSourceDragEvent dsde){
    }

    public void dragOver(DragSourceDragEvent dsde){
    }

    public void dropActionChanged(DragSourceDragEvent dsde){
    }

    public void dragExit(DragSourceEvent dse){
    }

    public void dragDropEnd(DragSourceDropEvent dsde){
    }

    public void dragMouseMoved(DragSourceDragEvent dsde){
    }
}
