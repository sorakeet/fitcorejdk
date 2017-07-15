/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import javax.swing.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public interface ComboPopup{
    public void show();

    public void hide();

    public boolean isVisible();

    public JList getList();

    public MouseListener getMouseListener();

    public MouseMotionListener getMouseMotionListener();

    public KeyListener getKeyListener();

    public void uninstallingUI();
}
