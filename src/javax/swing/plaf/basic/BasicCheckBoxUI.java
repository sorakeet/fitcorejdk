/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import sun.awt.AppContext;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

public class BasicCheckBoxUI extends BasicRadioButtonUI{
    private static final Object BASIC_CHECK_BOX_UI_KEY=new Object();
    private final static String propertyPrefix="CheckBox"+".";

    // ********************************
    //            Create PLAF
    // ********************************
    public static ComponentUI createUI(JComponent b){
        AppContext appContext=AppContext.getAppContext();
        BasicCheckBoxUI checkboxUI=
                (BasicCheckBoxUI)appContext.get(BASIC_CHECK_BOX_UI_KEY);
        if(checkboxUI==null){
            checkboxUI=new BasicCheckBoxUI();
            appContext.put(BASIC_CHECK_BOX_UI_KEY,checkboxUI);
        }
        return checkboxUI;
    }

    public String getPropertyPrefix(){
        return propertyPrefix;
    }
}
