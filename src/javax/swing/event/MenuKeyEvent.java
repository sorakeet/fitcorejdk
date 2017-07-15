/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class MenuKeyEvent extends KeyEvent{
    private MenuElement path[];
    private MenuSelectionManager manager;

    public MenuKeyEvent(Component source,int id,long when,int modifiers,
                        int keyCode,char keyChar,
                        MenuElement p[],MenuSelectionManager m){
        super(source,id,when,modifiers,keyCode,keyChar);
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
