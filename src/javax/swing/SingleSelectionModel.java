/**
 * Copyright (c) 1997, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.ChangeListener;

public interface SingleSelectionModel{
    public int getSelectedIndex();

    public void setSelectedIndex(int index);

    public void clearSelection();

    public boolean isSelected();

    void addChangeListener(ChangeListener listener);

    void removeChangeListener(ChangeListener listener);
}
