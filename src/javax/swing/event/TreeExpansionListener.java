/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventListener;

public interface TreeExpansionListener extends EventListener{
    public void treeExpanded(TreeExpansionEvent event);

    public void treeCollapsed(TreeExpansionEvent event);
}
