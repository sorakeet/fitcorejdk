/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import sun.awt.AppContext;
import sun.awt.SunToolkit;

import java.awt.*;

public class FocusEvent extends ComponentEvent{
    public static final int FOCUS_FIRST=1004;
    public static final int FOCUS_LAST=1005;
    public static final int FOCUS_GAINED=FOCUS_FIRST; //Event.GOT_FOCUS
    public static final int FOCUS_LOST=1+FOCUS_FIRST; //Event.LOST_FOCUS
    private static final long serialVersionUID=523753786457416396L;
    boolean temporary;
    transient Component opposite;

    public FocusEvent(Component source,int id){
        this(source,id,false);
    }

    public FocusEvent(Component source,int id,boolean temporary){
        this(source,id,temporary,null);
    }

    public FocusEvent(Component source,int id,boolean temporary,
                      Component opposite){
        super(source,id);
        this.temporary=temporary;
        this.opposite=opposite;
    }

    public boolean isTemporary(){
        return temporary;
    }

    public String paramString(){
        String typeStr;
        switch(id){
            case FOCUS_GAINED:
                typeStr="FOCUS_GAINED";
                break;
            case FOCUS_LOST:
                typeStr="FOCUS_LOST";
                break;
            default:
                typeStr="unknown type";
        }
        return typeStr+(temporary?",temporary":",permanent")+
                ",opposite="+getOppositeComponent();
    }

    public Component getOppositeComponent(){
        if(opposite==null){
            return null;
        }
        return (SunToolkit.targetToAppContext(opposite)==
                AppContext.getAppContext())
                ?opposite
                :null;
    }
}
