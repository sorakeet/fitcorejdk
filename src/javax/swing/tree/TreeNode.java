/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.tree;

import java.util.Enumeration;

public interface TreeNode{
    TreeNode getChildAt(int childIndex);

    int getChildCount();

    TreeNode getParent();

    int getIndex(TreeNode node);

    boolean getAllowsChildren();

    boolean isLeaf();

    Enumeration children();
}
