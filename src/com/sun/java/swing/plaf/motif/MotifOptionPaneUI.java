/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.motif;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.*;

public class MotifOptionPaneUI extends BasicOptionPaneUI{
    public static ComponentUI createUI(JComponent x){
        return new MotifOptionPaneUI();
    }

    public Dimension getMinimumOptionPaneSize(){
        return null;
    }

    protected void addIcon(Container top){
        /** Create the icon. */
        Icon sideIcon=getIcon();
        if(sideIcon!=null){
            JLabel iconLabel=new JLabel(sideIcon);
            iconLabel.setVerticalAlignment(SwingConstants.CENTER);
            top.add(iconLabel,"West");
        }
    }

    protected Container createSeparator(){
        return new JPanel(){
            public void paint(Graphics g){
                int width=getWidth();
                g.setColor(Color.darkGray);
                g.drawLine(0,0,width,0);
                g.setColor(Color.white);
                g.drawLine(0,1,width,1);
            }

            public Dimension getPreferredSize(){
                return new Dimension(10,2);
            }
        };
    }

    protected Container createButtonArea(){
        Container b=super.createButtonArea();
        if(b!=null&&b.getLayout() instanceof ButtonAreaLayout){
            ((ButtonAreaLayout)b.getLayout()).setCentersChildren(false);
        }
        return b;
    }
}
