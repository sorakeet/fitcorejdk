/**
 * Copyright (c) 1997, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicDesktopIconUI;
import java.awt.*;

public class WindowsDesktopIconUI extends BasicDesktopIconUI{
    private int width;

    public static ComponentUI createUI(JComponent c){
        return new WindowsDesktopIconUI();
    }

    public void installUI(JComponent c){
        super.installUI(c);
        c.setOpaque(XPStyle.getXP()==null);
    }

    // Uninstall the listeners added by the WindowsInternalFrameTitlePane
    public void uninstallUI(JComponent c){
        WindowsInternalFrameTitlePane thePane=
                (WindowsInternalFrameTitlePane)iconPane;
        super.uninstallUI(c);
        thePane.uninstallListeners();
    }

    protected void installComponents(){
        iconPane=new WindowsInternalFrameTitlePane(frame);
        desktopIcon.setLayout(new BorderLayout());
        desktopIcon.add(iconPane,BorderLayout.CENTER);
        if(XPStyle.getXP()!=null){
            desktopIcon.setBorder(null);
        }
    }

    public void installDefaults(){
        super.installDefaults();
        width=UIManager.getInt("DesktopIcon.width");
    }

    public Dimension getPreferredSize(JComponent c){
        // Windows desktop icons can not be resized.  Therefore, we should
        // always return the minimum size of the desktop icon. See
        // getMinimumSize(JComponent c).
        return getMinimumSize(c);
    }

    public Dimension getMinimumSize(JComponent c){
        Dimension dim=super.getMinimumSize(c);
        dim.width=width;
        return dim;
    }
}
