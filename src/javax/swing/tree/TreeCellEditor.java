/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.tree;

import javax.swing.*;
import java.awt.*;

public interface TreeCellEditor extends CellEditor{
    Component getTreeCellEditorComponent(JTree tree,Object value,
                                         boolean isSelected,boolean expanded,
                                         boolean leaf,int row);
}
