/**
 * Copyright (c) 2001, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import javax.swing.*;
import java.awt.*;

class WindowsPopupWindow extends JWindow{
    static final int UNDEFINED_WINDOW_TYPE=0;
    static final int TOOLTIP_WINDOW_TYPE=1;
    static final int MENU_WINDOW_TYPE=2;
    static final int SUBMENU_WINDOW_TYPE=3;
    static final int POPUPMENU_WINDOW_TYPE=4;
    static final int COMBOBOX_POPUP_WINDOW_TYPE=5;
    private int windowType;

    WindowsPopupWindow(Window parent){
        super(parent);
        setFocusableWindowState(false);
    }

    int getWindowType(){
        return windowType;
    }

    void setWindowType(int type){
        windowType=type;
    }

    public void update(Graphics g){
        paint(g);
    }

    public void show(){
        super.show();
        this.pack();
    }

    public void hide(){
        super.hide();
        /** We need to call removeNotify() here because hide() does
         * something only if Component.visible is true. When the app
         * frame is miniaturized, the parent frame of this frame is
         * invisible, causing AWT to believe that this frame
         *  is invisible and causing hide() to do nothing
         */
        removeNotify();
    }
}
