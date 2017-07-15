/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.metal;

import sun.awt.AppContext;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLabelUI;
import java.awt.*;

public class MetalLabelUI extends BasicLabelUI{
    private static final Object METAL_LABEL_UI_KEY=new Object();
    protected static MetalLabelUI metalLabelUI=new MetalLabelUI();

    public static ComponentUI createUI(JComponent c){
        if(System.getSecurityManager()!=null){
            AppContext appContext=AppContext.getAppContext();
            MetalLabelUI safeMetalLabelUI=
                    (MetalLabelUI)appContext.get(METAL_LABEL_UI_KEY);
            if(safeMetalLabelUI==null){
                safeMetalLabelUI=new MetalLabelUI();
                appContext.put(METAL_LABEL_UI_KEY,safeMetalLabelUI);
            }
            return safeMetalLabelUI;
        }
        return metalLabelUI;
    }

    protected void paintDisabledText(JLabel l,Graphics g,String s,int textX,int textY){
        int mnemIndex=l.getDisplayedMnemonicIndex();
        g.setColor(UIManager.getColor("Label.disabledForeground"));
        SwingUtilities2.drawStringUnderlineCharAt(l,g,s,mnemIndex,
                textX,textY);
    }
}
