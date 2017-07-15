/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventListener;

public interface TableColumnModelListener extends EventListener{
    public void columnAdded(TableColumnModelEvent e);

    public void columnRemoved(TableColumnModelEvent e);

    public void columnMoved(TableColumnModelEvent e);

    public void columnMarginChanged(ChangeEvent e);

    public void columnSelectionChanged(ListSelectionEvent e);
}
