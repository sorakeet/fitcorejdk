/**
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import com.sun.java.swing.plaf.windows.TMSchema.Part;
import com.sun.java.swing.plaf.windows.TMSchema.State;
import com.sun.java.swing.plaf.windows.XPStyle.Skin;
import sun.swing.StringUIClientPropertyKey;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;
import java.awt.*;

import static sun.swing.SwingUtilities2.BASICMENUITEMUI_MAX_TEXT_OFFSET;

public class WindowsPopupMenuUI extends BasicPopupMenuUI{
    static final Object GUTTER_OFFSET_KEY=
            new StringUIClientPropertyKey("GUTTER_OFFSET_KEY");
    static MnemonicListener mnemonicListener=null;

    public static ComponentUI createUI(JComponent c){
        return new WindowsPopupMenuUI();
    }

    static int getSpanBeforeGutter(){
        return 3;
    }

    public void installListeners(){
        super.installListeners();
        if(!UIManager.getBoolean("Button.showMnemonics")&&
                mnemonicListener==null){
            mnemonicListener=new MnemonicListener();
            MenuSelectionManager.defaultManager().
                    addChangeListener(mnemonicListener);
        }
    }

    public Popup getPopup(JPopupMenu popupMenu,int x,int y){
        PopupFactory popupFactory=PopupFactory.getSharedInstance();
        return popupFactory.getPopup(popupMenu.getInvoker(),popupMenu,x,y);
    }

    @Override
    public void paint(Graphics g,JComponent c){
        XPStyle xp=XPStyle.getXP();
        if(WindowsMenuItemUI.isVistaPainting(xp)){
            Skin skin=xp.getSkin(c,Part.MP_POPUPBACKGROUND);
            skin.paintSkin(g,0,0,c.getWidth(),c.getHeight(),State.NORMAL);
            int textOffset=getTextOffset(c);
            if(textOffset>=0
                    /** paint gutter only for leftToRight case */
                    &&isLeftToRight(c)){
                skin=xp.getSkin(c,Part.MP_POPUPGUTTER);
                int gutterWidth=getGutterWidth();
                int gutterOffset=
                        textOffset-getSpanAfterGutter()-gutterWidth;
                c.putClientProperty(GUTTER_OFFSET_KEY,
                        Integer.valueOf(gutterOffset));
                Insets insets=c.getInsets();
                skin.paintSkin(g,gutterOffset,insets.top,
                        gutterWidth,c.getHeight()-insets.bottom-insets.top,
                        State.NORMAL);
            }else{
                if(c.getClientProperty(GUTTER_OFFSET_KEY)!=null){
                    c.putClientProperty(GUTTER_OFFSET_KEY,null);
                }
            }
        }else{
            super.paint(g,c);
        }
    }

    static int getTextOffset(JComponent c){
        int rv=-1;
        Object maxTextOffset=
                c.getClientProperty(BASICMENUITEMUI_MAX_TEXT_OFFSET);
        if(maxTextOffset instanceof Integer){
            /**
             * this is in JMenuItem coordinates.
             * Let's assume all the JMenuItem have the same offset along X.
             */
            rv=(Integer)maxTextOffset;
            int menuItemOffset=0;
            Component component=c.getComponent(0);
            if(component!=null){
                menuItemOffset=component.getX();
            }
            rv+=menuItemOffset;
        }
        return rv;
    }

    static int getSpanAfterGutter(){
        return 3;
    }

    static int getGutterWidth(){
        int rv=2;
        XPStyle xp=XPStyle.getXP();
        if(xp!=null){
            Skin skin=xp.getSkin(null,Part.MP_POPUPGUTTER);
            rv=skin.getWidth();
        }
        return rv;
    }

    private static boolean isLeftToRight(JComponent c){
        boolean leftToRight=true;
        for(int i=c.getComponentCount()-1;i>=0&&leftToRight;i--){
            leftToRight=
                    c.getComponent(i).getComponentOrientation().isLeftToRight();
        }
        return leftToRight;
    }

    static class MnemonicListener implements ChangeListener{
        JRootPane repaintRoot=null;

        public void stateChanged(ChangeEvent ev){
            MenuSelectionManager msm=(MenuSelectionManager)ev.getSource();
            MenuElement[] path=msm.getSelectedPath();
            if(path.length==0){
                if(!WindowsLookAndFeel.isMnemonicHidden()){
                    // menu was canceled -- hide mnemonics
                    WindowsLookAndFeel.setMnemonicHidden(true);
                    if(repaintRoot!=null){
                        Window win=
                                SwingUtilities.getWindowAncestor(repaintRoot);
                        WindowsGraphicsUtils.repaintMnemonicsInWindow(win);
                    }
                }
            }else{
                Component c=(Component)path[0];
                if(c instanceof JPopupMenu) c=((JPopupMenu)c).getInvoker();
                repaintRoot=SwingUtilities.getRootPane(c);
            }
        }
    }
}
