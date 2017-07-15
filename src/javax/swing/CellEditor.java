/**
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.CellEditorListener;
import java.util.EventObject;

public interface CellEditor{
    public Object getCellEditorValue();

    public boolean isCellEditable(EventObject anEvent);

    public boolean shouldSelectCell(EventObject anEvent);

    public boolean stopCellEditing();

    public void cancelCellEditing();

    public void addCellEditorListener(CellEditorListener l);

    public void removeCellEditorListener(CellEditorListener l);
}
