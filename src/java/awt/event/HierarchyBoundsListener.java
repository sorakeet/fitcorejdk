/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.util.EventListener;

public interface HierarchyBoundsListener extends EventListener{
    public void ancestorMoved(HierarchyEvent e);

    public void ancestorResized(HierarchyEvent e);
}
