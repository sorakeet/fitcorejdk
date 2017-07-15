/**
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

public interface MutableComboBoxModel<E> extends ComboBoxModel<E>{
    public void addElement(E item);

    public void removeElement(Object obj);

    public void insertElementAt(E item,int index);

    public void removeElementAt(int index);
}
