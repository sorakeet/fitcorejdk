/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.awt.*;

public class ItemEvent extends AWTEvent{
    public static final int ITEM_FIRST=701;
    public static final int ITEM_LAST=701;
    public static final int ITEM_STATE_CHANGED=ITEM_FIRST; //Event.LIST_SELECT
    public static final int SELECTED=1;
    public static final int DESELECTED=2;
    private static final long serialVersionUID=-608708132447206933L;
    Object item;
    int stateChange;

    public ItemEvent(ItemSelectable source,int id,Object item,int stateChange){
        super(source,id);
        this.item=item;
        this.stateChange=stateChange;
    }

    public ItemSelectable getItemSelectable(){
        return (ItemSelectable)source;
    }

    public Object getItem(){
        return item;
    }

    public int getStateChange(){
        return stateChange;
    }

    public String paramString(){
        String typeStr;
        switch(id){
            case ITEM_STATE_CHANGED:
                typeStr="ITEM_STATE_CHANGED";
                break;
            default:
                typeStr="unknown type";
        }
        String stateStr;
        switch(stateChange){
            case SELECTED:
                stateStr="SELECTED";
                break;
            case DESELECTED:
                stateStr="DESELECTED";
                break;
            default:
                stateStr="unknown type";
        }
        return typeStr+",item="+item+",stateChange="+stateStr;
    }
}
