/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.tree;

import javax.swing.event.TreeSelectionListener;
import java.beans.PropertyChangeListener;

public interface TreeSelectionModel{
    public static final int SINGLE_TREE_SELECTION=1;
    public static final int CONTIGUOUS_TREE_SELECTION=2;
    public static final int DISCONTIGUOUS_TREE_SELECTION=4;

    int getSelectionMode();

    void setSelectionMode(int mode);

    void addSelectionPath(TreePath path);

    void addSelectionPaths(TreePath[] paths);

    void removeSelectionPath(TreePath path);

    void removeSelectionPaths(TreePath[] paths);

    TreePath getSelectionPath();

    void setSelectionPath(TreePath path);

    TreePath[] getSelectionPaths();

    void setSelectionPaths(TreePath[] paths);

    int getSelectionCount();

    boolean isPathSelected(TreePath path);

    boolean isSelectionEmpty();

    void clearSelection();

    RowMapper getRowMapper();

    void setRowMapper(RowMapper newMapper);

    int[] getSelectionRows();

    int getMinSelectionRow();

    int getMaxSelectionRow();

    boolean isRowSelected(int row);

    void resetRowSelection();

    int getLeadSelectionRow();

    TreePath getLeadSelectionPath();

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);

    void addTreeSelectionListener(TreeSelectionListener x);

    void removeTreeSelectionListener(TreeSelectionListener x);
}
