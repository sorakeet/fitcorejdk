/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd.peer;

import java.awt.dnd.DropTarget;

public interface DropTargetPeer{
    void addDropTarget(DropTarget dt);

    void removeDropTarget(DropTarget dt);
}
