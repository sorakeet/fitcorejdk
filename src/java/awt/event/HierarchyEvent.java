/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.awt.*;

public class HierarchyEvent extends AWTEvent{
    public static final int HIERARCHY_FIRST=1400; // 1300 used by sun.awt.windows.ModalityEvent
    public static final int HIERARCHY_CHANGED=HIERARCHY_FIRST;
    public static final int ANCESTOR_MOVED=1+HIERARCHY_FIRST;
    public static final int ANCESTOR_RESIZED=2+HIERARCHY_FIRST;
    public static final int HIERARCHY_LAST=ANCESTOR_RESIZED;
    public static final int PARENT_CHANGED=0x1;
    public static final int DISPLAYABILITY_CHANGED=0x2;
    public static final int SHOWING_CHANGED=0x4;
    private static final long serialVersionUID=-5337576970038043990L;
    Component changed;
    Container changedParent;
    long changeFlags;

    public HierarchyEvent(Component source,int id,Component changed,
                          Container changedParent){
        super(source,id);
        this.changed=changed;
        this.changedParent=changedParent;
    }

    public HierarchyEvent(Component source,int id,Component changed,
                          Container changedParent,long changeFlags){
        super(source,id);
        this.changed=changed;
        this.changedParent=changedParent;
        this.changeFlags=changeFlags;
    }

    public Component getComponent(){
        return (source instanceof Component)?(Component)source:null;
    }

    public Component getChanged(){
        return changed;
    }

    public Container getChangedParent(){
        return changedParent;
    }

    public long getChangeFlags(){
        return changeFlags;
    }

    public String paramString(){
        String typeStr;
        switch(id){
            case ANCESTOR_MOVED:
                typeStr="ANCESTOR_MOVED ("+changed+","+changedParent+")";
                break;
            case ANCESTOR_RESIZED:
                typeStr="ANCESTOR_RESIZED ("+changed+","+changedParent+")";
                break;
            case HIERARCHY_CHANGED:{
                typeStr="HIERARCHY_CHANGED (";
                boolean first=true;
                if((changeFlags&PARENT_CHANGED)!=0){
                    first=false;
                    typeStr+="PARENT_CHANGED";
                }
                if((changeFlags&DISPLAYABILITY_CHANGED)!=0){
                    if(first){
                        first=false;
                    }else{
                        typeStr+=",";
                    }
                    typeStr+="DISPLAYABILITY_CHANGED";
                }
                if((changeFlags&SHOWING_CHANGED)!=0){
                    if(first){
                        first=false;
                    }else{
                        typeStr+=",";
                    }
                    typeStr+="SHOWING_CHANGED";
                }
                if(!first){
                    typeStr+=",";
                }
                typeStr+=changed+","+changedParent+")";
                break;
            }
            default:
                typeStr="unknown type";
        }
        return typeStr;
    }
}
