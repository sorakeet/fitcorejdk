/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Hashtable;

public abstract class TextAction extends AbstractAction{
    public TextAction(String name){
        super(name);
    }

    public static final Action[] augmentList(Action[] list1,Action[] list2){
        Hashtable<String,Action> h=new Hashtable<String,Action>();
        for(Action a : list1){
            String value=(String)a.getValue(Action.NAME);
            h.put((value!=null?value:""),a);
        }
        for(Action a : list2){
            String value=(String)a.getValue(Action.NAME);
            h.put((value!=null?value:""),a);
        }
        Action[] actions=new Action[h.size()];
        int index=0;
        for(Enumeration e=h.elements();e.hasMoreElements();){
            actions[index++]=(Action)e.nextElement();
        }
        return actions;
    }

    protected final JTextComponent getTextComponent(ActionEvent e){
        if(e!=null){
            Object o=e.getSource();
            if(o instanceof JTextComponent){
                return (JTextComponent)o;
            }
        }
        return getFocusedComponent();
    }

    protected final JTextComponent getFocusedComponent(){
        return JTextComponent.getFocusedComponent();
    }
}
