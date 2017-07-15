/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf;

import javax.swing.*;

public abstract class ComboBoxUI extends ComponentUI{
    public abstract void setPopupVisible(JComboBox c,boolean v);

    public abstract boolean isPopupVisible(JComboBox c);

    public abstract boolean isFocusTraversable(JComboBox c);
}
