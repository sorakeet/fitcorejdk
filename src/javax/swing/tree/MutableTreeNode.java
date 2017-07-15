/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.tree;

public interface MutableTreeNode extends TreeNode{
    void insert(MutableTreeNode child,int index);

    void remove(int index);

    void remove(MutableTreeNode node);

    void setUserObject(Object object);

    void removeFromParent();

    void setParent(MutableTreeNode newParent);
}
