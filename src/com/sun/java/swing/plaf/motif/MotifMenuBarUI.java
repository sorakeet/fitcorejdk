/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.motif;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuBarUI;
//REMIND

public class MotifMenuBarUI extends BasicMenuBarUI{
    public static ComponentUI createUI(JComponent x){
        return new MotifMenuBarUI();
    }
} // end class
