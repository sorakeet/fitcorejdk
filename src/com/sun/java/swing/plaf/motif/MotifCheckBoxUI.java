/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.motif;

import sun.awt.AppContext;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

public class MotifCheckBoxUI extends MotifRadioButtonUI{
    private static final Object MOTIF_CHECK_BOX_UI_KEY=new Object();
    private final static String propertyPrefix="CheckBox"+".";
    private boolean defaults_initialized=false;

    // ********************************
    //         Create PLAF
    // ********************************
    public static ComponentUI createUI(JComponent c){
        AppContext appContext=AppContext.getAppContext();
        MotifCheckBoxUI motifCheckBoxUI=
                (MotifCheckBoxUI)appContext.get(MOTIF_CHECK_BOX_UI_KEY);
        if(motifCheckBoxUI==null){
            motifCheckBoxUI=new MotifCheckBoxUI();
            appContext.put(MOTIF_CHECK_BOX_UI_KEY,motifCheckBoxUI);
        }
        return motifCheckBoxUI;
    }

    // ********************************
    //          Defaults
    // ********************************
    public void installDefaults(AbstractButton b){
        super.installDefaults(b);
        if(!defaults_initialized){
            icon=UIManager.getIcon(getPropertyPrefix()+"icon");
            defaults_initialized=true;
        }
    }

    public String getPropertyPrefix(){
        return propertyPrefix;
    }

    protected void uninstallDefaults(AbstractButton b){
        super.uninstallDefaults(b);
        defaults_initialized=false;
    }
}
