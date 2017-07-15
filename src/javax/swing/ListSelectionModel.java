/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.ListSelectionListener;

public interface ListSelectionModel{
    int SINGLE_SELECTION=0;
    int SINGLE_INTERVAL_SELECTION=1;
    int MULTIPLE_INTERVAL_SELECTION=2;

    void setSelectionInterval(int index0,int index1);

    void addSelectionInterval(int index0,int index1);

    void removeSelectionInterval(int index0,int index1);

    int getMinSelectionIndex();

    int getMaxSelectionIndex();

    boolean isSelectedIndex(int index);

    int getAnchorSelectionIndex();

    void setAnchorSelectionIndex(int index);

    int getLeadSelectionIndex();

    void setLeadSelectionIndex(int index);

    void clearSelection();

    boolean isSelectionEmpty();

    void insertIndexInterval(int index,int length,boolean before);

    void removeIndexInterval(int index0,int index1);

    boolean getValueIsAdjusting();

    void setValueIsAdjusting(boolean valueIsAdjusting);

    int getSelectionMode();

    void setSelectionMode(int selectionMode);

    void addListSelectionListener(ListSelectionListener x);

    void removeListSelectionListener(ListSelectionListener x);
}
