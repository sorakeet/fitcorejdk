/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.table;

import javax.swing.*;
import javax.swing.event.TableColumnModelListener;
import java.util.Enumeration;

public interface TableColumnModel{
//
// Modifying the model
//

    public void addColumn(TableColumn aColumn);

    public void removeColumn(TableColumn column);

    public void moveColumn(int columnIndex,int newIndex);

    public int getColumnCount();
//
// Querying the model
//

    public Enumeration<TableColumn> getColumns();

    public int getColumnIndex(Object columnIdentifier);

    public TableColumn getColumn(int columnIndex);

    public int getColumnMargin();

    public void setColumnMargin(int newMargin);

    public int getColumnIndexAtX(int xPosition);

    public int getTotalColumnWidth();
//
// Selection
//

    public boolean getColumnSelectionAllowed();

    public void setColumnSelectionAllowed(boolean flag);

    public int[] getSelectedColumns();

    public int getSelectedColumnCount();

    public ListSelectionModel getSelectionModel();

    public void setSelectionModel(ListSelectionModel newModel);
//
// Listener
//

    public void addColumnModelListener(TableColumnModelListener x);

    public void removeColumnModelListener(TableColumnModelListener x);
}
