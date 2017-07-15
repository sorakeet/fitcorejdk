/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import javax.swing.tree.ExpandVetoException;
import java.util.EventListener;

public interface TreeWillExpandListener extends EventListener{
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException;

    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException;
}
