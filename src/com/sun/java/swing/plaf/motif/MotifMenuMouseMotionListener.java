/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.motif;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

class MotifMenuMouseMotionListener implements MouseMotionListener{
    public void mouseDragged(MouseEvent e){
        MenuSelectionManager.defaultManager().processMouseEvent(e);
    }

    public void mouseMoved(MouseEvent e){
        MenuSelectionManager.defaultManager().processMouseEvent(e);
    }
}
