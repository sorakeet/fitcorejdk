/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.table;

import javax.swing.*;
import java.awt.*;

public interface TableCellRenderer{
    Component getTableCellRendererComponent(JTable table,Object value,
                                            boolean isSelected,boolean hasFocus,
                                            int row,int column);
}
