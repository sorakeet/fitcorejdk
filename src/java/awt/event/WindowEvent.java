/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import sun.awt.AppContext;
import sun.awt.SunToolkit;

import java.awt.*;
import java.lang.annotation.Native;

public class WindowEvent extends ComponentEvent{
    public static final int WINDOW_FIRST=200;
    @Native
    public static final int WINDOW_OPENED=WINDOW_FIRST; // 200
    @Native
    public static final int WINDOW_CLOSING=1+WINDOW_FIRST; //Event.WINDOW_DESTROY
    @Native
    public static final int WINDOW_CLOSED=2+WINDOW_FIRST;
    @Native
    public static final int WINDOW_ICONIFIED=3+WINDOW_FIRST; //Event.WINDOW_ICONIFY
    @Native
    public static final int WINDOW_DEICONIFIED=4+WINDOW_FIRST; //Event.WINDOW_DEICONIFY
    @Native
    public static final int WINDOW_ACTIVATED=5+WINDOW_FIRST;
    @Native
    public static final int WINDOW_DEACTIVATED=6+WINDOW_FIRST;
    @Native
    public static final int WINDOW_GAINED_FOCUS=7+WINDOW_FIRST;
    @Native
    public static final int WINDOW_LOST_FOCUS=8+WINDOW_FIRST;
    @Native
    public static final int WINDOW_STATE_CHANGED=9+WINDOW_FIRST;
    public static final int WINDOW_LAST=WINDOW_STATE_CHANGED;
    private static final long serialVersionUID=-1567959133147912127L;
    transient Window opposite;
    int oldState;
    int newState;

    public WindowEvent(Window source,int id,Window opposite){
        this(source,id,opposite,0,0);
    }

    public WindowEvent(Window source,int id,Window opposite,
                       int oldState,int newState){
        super(source,id);
        this.opposite=opposite;
        this.oldState=oldState;
        this.newState=newState;
    }

    public WindowEvent(Window source,int id,int oldState,int newState){
        this(source,id,null,oldState,newState);
    }

    public WindowEvent(Window source,int id){
        this(source,id,null,0,0);
    }

    public Window getWindow(){
        return (source instanceof Window)?(Window)source:null;
    }

    public int getOldState(){
        return oldState;
    }

    public int getNewState(){
        return newState;
    }

    public String paramString(){
        String typeStr;
        switch(id){
            case WINDOW_OPENED:
                typeStr="WINDOW_OPENED";
                break;
            case WINDOW_CLOSING:
                typeStr="WINDOW_CLOSING";
                break;
            case WINDOW_CLOSED:
                typeStr="WINDOW_CLOSED";
                break;
            case WINDOW_ICONIFIED:
                typeStr="WINDOW_ICONIFIED";
                break;
            case WINDOW_DEICONIFIED:
                typeStr="WINDOW_DEICONIFIED";
                break;
            case WINDOW_ACTIVATED:
                typeStr="WINDOW_ACTIVATED";
                break;
            case WINDOW_DEACTIVATED:
                typeStr="WINDOW_DEACTIVATED";
                break;
            case WINDOW_GAINED_FOCUS:
                typeStr="WINDOW_GAINED_FOCUS";
                break;
            case WINDOW_LOST_FOCUS:
                typeStr="WINDOW_LOST_FOCUS";
                break;
            case WINDOW_STATE_CHANGED:
                typeStr="WINDOW_STATE_CHANGED";
                break;
            default:
                typeStr="unknown type";
        }
        typeStr+=",opposite="+getOppositeWindow()
                +",oldState="+oldState+",newState="+newState;
        return typeStr;
    }

    public Window getOppositeWindow(){
        if(opposite==null){
            return null;
        }
        return (SunToolkit.targetToAppContext(opposite)==
                AppContext.getAppContext())
                ?opposite
                :null;
    }
}
