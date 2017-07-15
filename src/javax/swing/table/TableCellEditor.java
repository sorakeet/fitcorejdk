/**
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.table;

import javax.swing.*;
import java.awt.*;

public interface TableCellEditor extends CellEditor{
    Component getTableCellEditorComponent(JTable table,Object value,
                                          boolean isSelected,
                                          int row,int column);
}
