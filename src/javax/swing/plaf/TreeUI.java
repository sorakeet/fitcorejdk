/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;

public abstract class TreeUI extends ComponentUI{
    public abstract Rectangle getPathBounds(JTree tree,TreePath path);

    public abstract TreePath getPathForRow(JTree tree,int row);

    public abstract int getRowForPath(JTree tree,TreePath path);

    public abstract int getRowCount(JTree tree);

    public abstract TreePath getClosestPathForLocation(JTree tree,int x,
                                                       int y);

    public abstract boolean isEditing(JTree tree);

    public abstract boolean stopEditing(JTree tree);

    public abstract void cancelEditing(JTree tree);

    public abstract void startEditingAtPath(JTree tree,TreePath path);

    public abstract TreePath getEditingPath(JTree tree);
}
