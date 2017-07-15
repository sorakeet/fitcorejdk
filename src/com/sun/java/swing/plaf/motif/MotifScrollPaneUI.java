/**
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.motif;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollPaneUI;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MotifScrollPaneUI extends BasicScrollPaneUI{
    private final static Border vsbMarginBorderR=new EmptyBorder(0,4,0,0);
    private final static Border vsbMarginBorderL=new EmptyBorder(0,0,0,4);
    private final static Border hsbMarginBorder=new EmptyBorder(4,0,0,0);
    private CompoundBorder vsbBorder;
    private CompoundBorder hsbBorder;
    private PropertyChangeListener propertyChangeHandler;

    public static ComponentUI createUI(JComponent x){
        return new MotifScrollPaneUI();
    }

    @Override
    protected void installDefaults(JScrollPane scrollpane){
        super.installDefaults(scrollpane);
        JScrollBar vsb=scrollpane.getVerticalScrollBar();
        if(vsb!=null){
            if(MotifGraphicsUtils.isLeftToRight(scrollpane)){
                vsbBorder=new CompoundBorder(vsbMarginBorderR,
                        vsb.getBorder());
            }else{
                vsbBorder=new CompoundBorder(vsbMarginBorderL,
                        vsb.getBorder());
            }
            vsb.setBorder(vsbBorder);
        }
        JScrollBar hsb=scrollpane.getHorizontalScrollBar();
        if(hsb!=null){
            hsbBorder=new CompoundBorder(hsbMarginBorder,hsb.getBorder());
            hsb.setBorder(hsbBorder);
        }
    }

    @Override
    protected void installListeners(JScrollPane scrollPane){
        super.installListeners(scrollPane);
        propertyChangeHandler=createPropertyChangeHandler();
        scrollPane.addPropertyChangeListener(propertyChangeHandler);
    }

    private PropertyChangeListener createPropertyChangeHandler(){
        return new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent e){
                String propertyName=e.getPropertyName();
                if(propertyName.equals("componentOrientation")){
                    JScrollPane pane=(JScrollPane)e.getSource();
                    JScrollBar vsb=pane.getVerticalScrollBar();
                    if(vsb!=null&&vsbBorder!=null&&
                            vsb.getBorder()==vsbBorder){
                        // The Border on the verticall scrollbar matches
                        // what we installed, reset it.
                        if(MotifGraphicsUtils.isLeftToRight(pane)){
                            vsbBorder=new CompoundBorder(vsbMarginBorderR,
                                    vsbBorder.getInsideBorder());
                        }else{
                            vsbBorder=new CompoundBorder(vsbMarginBorderL,
                                    vsbBorder.getInsideBorder());
                        }
                        vsb.setBorder(vsbBorder);
                    }
                }
            }
        };
    }

    @Override
    protected void uninstallDefaults(JScrollPane c){
        super.uninstallDefaults(c);
        JScrollBar vsb=scrollpane.getVerticalScrollBar();
        if(vsb!=null){
            if(vsb.getBorder()==vsbBorder){
                vsb.setBorder(null);
            }
            vsbBorder=null;
        }
        JScrollBar hsb=scrollpane.getHorizontalScrollBar();
        if(hsb!=null){
            if(hsb.getBorder()==hsbBorder){
                hsb.setBorder(null);
            }
            hsbBorder=null;
        }
    }

    @Override
    protected void uninstallListeners(JComponent scrollPane){
        super.uninstallListeners(scrollPane);
        scrollPane.removePropertyChangeListener(propertyChangeHandler);
    }
}
