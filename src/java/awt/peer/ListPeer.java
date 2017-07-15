/**
 * Copyright (c) 1995, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface ListPeer extends ComponentPeer{
    int[] getSelectedIndexes();

    void add(String item,int index);

    void delItems(int start,int end);

    void removeAll();

    void select(int index);

    void deselect(int index);

    void makeVisible(int index);

    void setMultipleMode(boolean m);

    Dimension getPreferredSize(int rows);

    Dimension getMinimumSize(int rows);
}
