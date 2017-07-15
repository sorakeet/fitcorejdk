/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicDesktopPaneUI;

public class WindowsDesktopPaneUI extends BasicDesktopPaneUI{
    public static ComponentUI createUI(JComponent c){
        return new WindowsDesktopPaneUI();
    }

    protected void installDefaults(){
        super.installDefaults();
    }

    protected void installDesktopManager(){
        desktopManager=desktop.getDesktopManager();
        if(desktopManager==null){
            desktopManager=new WindowsDesktopManager();
            desktop.setDesktopManager(desktopManager);
        }
    }

    protected void installKeyboardActions(){
        super.installKeyboardActions();
        // Request focus if it isn't set.
        if(!desktop.requestDefaultFocus()){
            desktop.requestFocus();
        }
    }
}
