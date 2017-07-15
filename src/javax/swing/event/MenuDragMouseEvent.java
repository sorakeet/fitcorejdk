/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

@SuppressWarnings("serial")
public class MenuDragMouseEvent extends MouseEvent{
    private MenuElement path[];
    private MenuSelectionManager manager;

    public MenuDragMouseEvent(Component source,int id,long when,
                              int modifiers,int x,int y,int clickCount,
                              boolean popupTrigger,MenuElement p[],
                              MenuSelectionManager m){
        super(source,id,when,modifiers,x,y,clickCount,popupTrigger);
        path=p;
        manager=m;
    }

    public MenuDragMouseEvent(Component source,int id,long when,
                              int modifiers,int x,int y,int xAbs,
                              int yAbs,int clickCount,
                              boolean popupTrigger,MenuElement p[],
                              MenuSelectionManager m){
        super(source,id,when,modifiers,x,y,xAbs,yAbs,clickCount,
                popupTrigger,MouseEvent.NOBUTTON);
        path=p;
        manager=m;
    }

    public MenuElement[] getPath(){
        return path;
    }

    public MenuSelectionManager getMenuSelectionManager(){
        return manager;
    }
}
