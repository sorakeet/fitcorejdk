/**
 * Copyright (c) 1996, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.event.ItemListener;

public interface ItemSelectable{
    public Object[] getSelectedObjects();

    public void addItemListener(ItemListener l);

    public void removeItemListener(ItemListener l);
}
