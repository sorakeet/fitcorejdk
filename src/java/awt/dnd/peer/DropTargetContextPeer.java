/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd.peer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.InvalidDnDOperationException;

public interface DropTargetContextPeer{
    int getTargetActions();

    void setTargetActions(int actions);

    DropTarget getDropTarget();

    DataFlavor[] getTransferDataFlavors();

    Transferable getTransferable() throws InvalidDnDOperationException;

    boolean isTransferableJVMLocal();

    void acceptDrag(int dragAction);

    void rejectDrag();

    void acceptDrop(int dropAction);

    void rejectDrop();

    void dropComplete(boolean success);
}
