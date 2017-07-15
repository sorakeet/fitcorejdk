/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.util.EventListener;

public interface ContainerListener extends EventListener{
    public void componentAdded(ContainerEvent e);

    public void componentRemoved(ContainerEvent e);
}
