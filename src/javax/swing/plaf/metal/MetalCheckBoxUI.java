/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.metal;

import sun.awt.AppContext;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

public class MetalCheckBoxUI extends MetalRadioButtonUI{
    // NOTE: MetalCheckBoxUI inherts from MetalRadioButtonUI instead
    // of BasicCheckBoxUI because we want to pick up all the
    // painting changes made in MetalRadioButtonUI.
    private static final Object METAL_CHECK_BOX_UI_KEY=new Object();
    private final static String propertyPrefix="CheckBox"+".";
    private boolean defaults_initialized=false;

    // ********************************
    //         Create PlAF
    // ********************************
    public static ComponentUI createUI(JComponent b){
        AppContext appContext=AppContext.getAppContext();
        MetalCheckBoxUI checkboxUI=
                (MetalCheckBoxUI)appContext.get(METAL_CHECK_BOX_UI_KEY);
        if(checkboxUI==null){
            checkboxUI=new MetalCheckBoxUI();
            appContext.put(METAL_CHECK_BOX_UI_KEY,checkboxUI);
        }
        return checkboxUI;
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
