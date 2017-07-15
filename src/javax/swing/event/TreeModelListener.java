/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventListener;

public interface TreeModelListener extends EventListener{
    void treeNodesChanged(TreeModelEvent e);

    void treeNodesInserted(TreeModelEvent e);

    void treeNodesRemoved(TreeModelEvent e);

    void treeStructureChanged(TreeModelEvent e);
}
