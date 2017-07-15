/**
 * Copyright (c) 1997, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventListener;

public interface ListDataListener extends EventListener{
    void intervalAdded(ListDataEvent e);

    void intervalRemoved(ListDataEvent e);

    void contentsChanged(ListDataEvent e);
}
