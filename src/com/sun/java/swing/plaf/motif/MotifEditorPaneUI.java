/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.motif;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicEditorPaneUI;
import javax.swing.text.Caret;

public class MotifEditorPaneUI extends BasicEditorPaneUI{
    public static ComponentUI createUI(JComponent c){
        return new MotifEditorPaneUI();
    }

    protected Caret createCaret(){
        return MotifTextUI.createCaret();
    }
}
