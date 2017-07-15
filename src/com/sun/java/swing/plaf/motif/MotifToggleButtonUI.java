/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.motif;

import sun.awt.AppContext;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;

public class MotifToggleButtonUI extends BasicToggleButtonUI{
    private static final Object MOTIF_TOGGLE_BUTTON_UI_KEY=new Object();
    protected Color selectColor;
    private boolean defaults_initialized=false;

    // ********************************
    //         Create PLAF
    // ********************************
    public static ComponentUI createUI(JComponent b){
        AppContext appContext=AppContext.getAppContext();
        MotifToggleButtonUI motifToggleButtonUI=
                (MotifToggleButtonUI)appContext.get(MOTIF_TOGGLE_BUTTON_UI_KEY);
        if(motifToggleButtonUI==null){
            motifToggleButtonUI=new MotifToggleButtonUI();
            appContext.put(MOTIF_TOGGLE_BUTTON_UI_KEY,motifToggleButtonUI);
        }
        return motifToggleButtonUI;
    }

    // ********************************
    //          Install Defaults
    // ********************************
    public void installDefaults(AbstractButton b){
        super.installDefaults(b);
        if(!defaults_initialized){
            selectColor=UIManager.getColor(getPropertyPrefix()+"select");
            defaults_initialized=true;
        }
        LookAndFeel.installProperty(b,"opaque",Boolean.FALSE);
    }

    protected void uninstallDefaults(AbstractButton b){
        super.uninstallDefaults(b);
        defaults_initialized=false;
    }
    // ********************************
    //          Default Accessors
    // ********************************

    // ********************************
    //         Paint Methods
    // ********************************
    protected void paintButtonPressed(Graphics g,AbstractButton b){
        if(b.isContentAreaFilled()){
            Color oldColor=g.getColor();
            Dimension size=b.getSize();
            Insets insets=b.getInsets();
            Insets margin=b.getMargin();
            if(b.getBackground() instanceof UIResource){
                g.setColor(getSelectColor());
            }
            g.fillRect(insets.left-margin.left,
                    insets.top-margin.top,
                    size.width-(insets.left-margin.left)-(insets.right-margin.right),
                    size.height-(insets.top-margin.top)-(insets.bottom-margin.bottom));
            g.setColor(oldColor);
        }
    }

    protected Color getSelectColor(){
        return selectColor;
    }

    public Insets getInsets(JComponent c){
        Border border=c.getBorder();
        Insets i=border!=null?border.getBorderInsets(c):new Insets(0,0,0,0);
        return i;
    }
}
