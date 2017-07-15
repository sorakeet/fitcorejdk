/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

public interface ButtonModel extends ItemSelectable{
    boolean isArmed();

    public void setArmed(boolean b);

    boolean isSelected();

    public void setSelected(boolean b);

    boolean isEnabled();

    public void setEnabled(boolean b);

    boolean isPressed();

    public void setPressed(boolean b);

    boolean isRollover();

    public void setRollover(boolean b);

    public int getMnemonic();

    public void setMnemonic(int key);

    public String getActionCommand();

    public void setActionCommand(String s);

    public void setGroup(ButtonGroup group);

    void addActionListener(ActionListener l);

    void removeActionListener(ActionListener l);

    void addItemListener(ItemListener l);

    void removeItemListener(ItemListener l);

    void addChangeListener(ChangeListener l);

    void removeChangeListener(ChangeListener l);
}
