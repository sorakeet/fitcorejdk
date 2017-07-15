/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.motif;

import sun.awt.AppContext;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLabelUI;

public class MotifLabelUI extends BasicLabelUI{
    private static final Object MOTIF_LABEL_UI_KEY=new Object();

    public static ComponentUI createUI(JComponent c){
        AppContext appContext=AppContext.getAppContext();
        MotifLabelUI motifLabelUI=
                (MotifLabelUI)appContext.get(MOTIF_LABEL_UI_KEY);
        if(motifLabelUI==null){
            motifLabelUI=new MotifLabelUI();
            appContext.put(MOTIF_LABEL_UI_KEY,motifLabelUI);
        }
        return motifLabelUI;
    }
}
