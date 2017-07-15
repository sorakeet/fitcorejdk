/**
 * Copyright (c) 1997, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.awt.*;

import static com.sun.java.swing.plaf.windows.TMSchema.Part;
import static com.sun.java.swing.plaf.windows.TMSchema.State;
import static com.sun.java.swing.plaf.windows.XPStyle.Skin;

public class WindowsInternalFrameUI extends BasicInternalFrameUI{
    XPStyle xp=XPStyle.getXP();

    public WindowsInternalFrameUI(JInternalFrame w){
        super(w);
    }

    public static ComponentUI createUI(JComponent b){
        return new WindowsInternalFrameUI((JInternalFrame)b);
    }

    public void installUI(JComponent c){
        super.installUI(c);
        LookAndFeel.installProperty(c,"opaque",
                xp==null?Boolean.TRUE:Boolean.FALSE);
    }

    public void installDefaults(){
        super.installDefaults();
        if(xp!=null){
            frame.setBorder(new XPBorder());
        }else{
            frame.setBorder(UIManager.getBorder("InternalFrame.border"));
        }
    }

    public void uninstallDefaults(){
        frame.setBorder(null);
        super.uninstallDefaults();
    }

    protected JComponent createNorthPane(JInternalFrame w){
        titlePane=new WindowsInternalFrameTitlePane(w);
        return titlePane;
    }

    protected DesktopManager createDesktopManager(){
        return new WindowsDesktopManager();
    }

    private class XPBorder extends AbstractBorder{
        private Skin leftSkin=xp.getSkin(frame,Part.WP_FRAMELEFT);
        private Skin rightSkin=xp.getSkin(frame,Part.WP_FRAMERIGHT);
        private Skin bottomSkin=xp.getSkin(frame,Part.WP_FRAMEBOTTOM);

        public void paintBorder(Component c,Graphics g,int x,int y,int width,int height){
            State state=((JInternalFrame)c).isSelected()?State.ACTIVE:State.INACTIVE;
            int topBorderHeight=(titlePane!=null)?titlePane.getSize().height:0;
            bottomSkin.paintSkin(g,0,height-bottomSkin.getHeight(),
                    width,bottomSkin.getHeight(),
                    state);
            leftSkin.paintSkin(g,0,topBorderHeight-1,
                    leftSkin.getWidth(),height-topBorderHeight-bottomSkin.getHeight()+2,
                    state);
            rightSkin.paintSkin(g,width-rightSkin.getWidth(),topBorderHeight-1,
                    rightSkin.getWidth(),height-topBorderHeight-bottomSkin.getHeight()+2,
                    state);
        }

        public Insets getBorderInsets(Component c,Insets insets){
            insets.top=4;
            insets.left=leftSkin.getWidth();
            insets.right=rightSkin.getWidth();
            insets.bottom=bottomSkin.getHeight();
            return insets;
        }

        public boolean isBorderOpaque(){
            return true;
        }
    }
}
