/**
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import com.sun.java.swing.plaf.windows.TMSchema.Part;
import com.sun.java.swing.plaf.windows.TMSchema.State;
import com.sun.java.swing.plaf.windows.XPStyle.Skin;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuItemUI;
import java.awt.*;

public class WindowsMenuItemUI extends BasicMenuItemUI{
    final WindowsMenuItemUIAccessor accessor=
            new WindowsMenuItemUIAccessor(){
                public JMenuItem getMenuItem(){
                    return menuItem;
                }

                public State getState(JMenuItem menuItem){
                    return WindowsMenuItemUI.getState(this,menuItem);
                }

                public Part getPart(JMenuItem menuItem){
                    return WindowsMenuItemUI.getPart(this,menuItem);
                }
            };

    public static ComponentUI createUI(JComponent c){
        return new WindowsMenuItemUI();
    }

    static State getState(WindowsMenuItemUIAccessor menuItemUI,JMenuItem menuItem){
        State state;
        ButtonModel model=menuItem.getModel();
        if(model.isArmed()){
            state=(model.isEnabled())?State.HOT:State.DISABLEDHOT;
        }else{
            state=(model.isEnabled())?State.NORMAL:State.DISABLED;
        }
        return state;
    }

    static Part getPart(WindowsMenuItemUIAccessor menuItemUI,JMenuItem menuItem){
        return Part.MP_POPUPITEM;
    }

    @Override
    protected void paintBackground(Graphics g,JMenuItem menuItem,
                                   Color bgColor){
        if(WindowsMenuItemUI.isVistaPainting()){
            WindowsMenuItemUI.paintBackground(accessor,g,menuItem,bgColor);
            return;
        }
        super.paintBackground(g,menuItem,bgColor);
    }

    protected void paintText(Graphics g,JMenuItem menuItem,
                             Rectangle textRect,String text){
        if(WindowsMenuItemUI.isVistaPainting()){
            WindowsMenuItemUI.paintText(accessor,g,menuItem,textRect,text);
            return;
        }
        ButtonModel model=menuItem.getModel();
        Color oldColor=g.getColor();
        if(model.isEnabled()&&
                (model.isArmed()||(menuItem instanceof JMenu&&
                        model.isSelected()))){
            g.setColor(selectionForeground); // Uses protected field.
        }
        WindowsGraphicsUtils.paintText(g,menuItem,textRect,text,0);
        g.setColor(oldColor);
    }

    static void paintText(WindowsMenuItemUIAccessor menuItemUI,Graphics g,
                          JMenuItem menuItem,Rectangle textRect,
                          String text){
        assert isVistaPainting();
        if(isVistaPainting()){
            State state=menuItemUI.getState(menuItem);
            /** part of it copied from WindowsGraphicsUtils.java */
            FontMetrics fm=SwingUtilities2.getFontMetrics(menuItem,g);
            int mnemIndex=menuItem.getDisplayedMnemonicIndex();
            // W2K Feature: Check to see if the Underscore should be rendered.
            if(WindowsLookAndFeel.isMnemonicHidden()==true){
                mnemIndex=-1;
            }
            WindowsGraphicsUtils.paintXPText(menuItem,
                    menuItemUI.getPart(menuItem),state,
                    g,textRect.x,
                    textRect.y+fm.getAscent(),
                    text,mnemIndex);
        }
    }

    static boolean isVistaPainting(){
        return isVistaPainting(XPStyle.getXP());
    }

    static boolean isVistaPainting(final XPStyle xp){
        return xp!=null&&xp.isSkinDefined(null,Part.MP_POPUPITEM);
    }

    static void paintBackground(WindowsMenuItemUIAccessor menuItemUI,
                                Graphics g,JMenuItem menuItem,Color bgColor){
        XPStyle xp=XPStyle.getXP();
        assert isVistaPainting(xp);
        if(isVistaPainting(xp)){
            int menuWidth=menuItem.getWidth();
            int menuHeight=menuItem.getHeight();
            if(menuItem.isOpaque()){
                Color oldColor=g.getColor();
                g.setColor(menuItem.getBackground());
                g.fillRect(0,0,menuWidth,menuHeight);
                g.setColor(oldColor);
            }
            Part part=menuItemUI.getPart(menuItem);
            Skin skin=xp.getSkin(menuItem,part);
            skin.paintSkin(g,0,0,
                    menuWidth,
                    menuHeight,
                    menuItemUI.getState(menuItem));
        }
    }
}
