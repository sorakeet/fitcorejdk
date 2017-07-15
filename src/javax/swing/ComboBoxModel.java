/**
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

public interface ComboBoxModel<E> extends ListModel<E>{
    Object getSelectedItem();

    void setSelectedItem(Object anItem);
}
