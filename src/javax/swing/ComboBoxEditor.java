/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;
import java.awt.event.ActionListener;

public interface ComboBoxEditor{
    public Component getEditorComponent();

    public Object getItem();

    public void setItem(Object anObject);

    public void selectAll();

    public void addActionListener(ActionListener l);

    public void removeActionListener(ActionListener l);
}
