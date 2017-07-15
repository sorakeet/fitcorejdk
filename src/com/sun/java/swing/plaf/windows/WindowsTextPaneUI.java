/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.Caret;

public class WindowsTextPaneUI extends BasicTextPaneUI{
    public static ComponentUI createUI(JComponent c){
        return new WindowsTextPaneUI();
    }

    protected Caret createCaret(){
        return new WindowsTextUI.WindowsCaret();
    }
}
