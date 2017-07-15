/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.tree;

import javax.swing.*;
import java.awt.*;

public interface TreeCellRenderer{
    Component getTreeCellRendererComponent(JTree tree,Object value,
                                           boolean selected,boolean expanded,
                                           boolean leaf,int row,boolean hasFocus);
}
