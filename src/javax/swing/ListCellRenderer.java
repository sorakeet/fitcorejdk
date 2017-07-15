/**
 * Copyright (c) 1997, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;

public interface ListCellRenderer<E>{
    Component getListCellRendererComponent(
            JList<? extends E> list,
            E value,
            int index,
            boolean isSelected,
            boolean cellHasFocus);
}
