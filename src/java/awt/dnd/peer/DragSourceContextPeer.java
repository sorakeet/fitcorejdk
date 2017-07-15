/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd.peer;

import java.awt.*;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.InvalidDnDOperationException;

public interface DragSourceContextPeer{
    void startDrag(DragSourceContext dsc,Cursor c,Image dragImage,Point imageOffset) throws InvalidDnDOperationException;

    Cursor getCursor();

    void setCursor(Cursor c) throws InvalidDnDOperationException;

    void transferablesFlavorsChanged();
}
