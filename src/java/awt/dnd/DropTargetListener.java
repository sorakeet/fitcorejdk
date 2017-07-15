/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd;

import java.util.EventListener;

public interface DropTargetListener extends EventListener{
    void dragEnter(DropTargetDragEvent dtde);

    void dragOver(DropTargetDragEvent dtde);

    void dropActionChanged(DropTargetDragEvent dtde);

    void dragExit(DropTargetEvent dte);

    void drop(DropTargetDropEvent dtde);
}
