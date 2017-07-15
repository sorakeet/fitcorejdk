/**
 * Copyright (c) 1997, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.ListDataListener;

public interface ListModel<E>{
    int getSize();

    E getElementAt(int index);

    void addListDataListener(ListDataListener l);

    void removeListDataListener(ListDataListener l);
}
