/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.awt.*;
import java.lang.annotation.Native;

public class ComponentEvent extends AWTEvent{
    public static final int COMPONENT_FIRST=100;
    public static final int COMPONENT_LAST=103;
    @Native
    public static final int COMPONENT_MOVED=COMPONENT_FIRST;
    @Native
    public static final int COMPONENT_RESIZED=1+COMPONENT_FIRST;
    @Native
    public static final int COMPONENT_SHOWN=2+COMPONENT_FIRST;
    @Native
    public static final int COMPONENT_HIDDEN=3+COMPONENT_FIRST;
    private static final long serialVersionUID=8101406823902992965L;

    public ComponentEvent(Component source,int id){
        super(source,id);
    }

    public Component getComponent(){
        return (source instanceof Component)?(Component)source:null;
    }

    public String paramString(){
        String typeStr;
        Rectangle b=(source!=null
                ?((Component)source).getBounds()
                :null);
        switch(id){
            case COMPONENT_SHOWN:
                typeStr="COMPONENT_SHOWN";
                break;
            case COMPONENT_HIDDEN:
                typeStr="COMPONENT_HIDDEN";
                break;
            case COMPONENT_MOVED:
                typeStr="COMPONENT_MOVED ("+
                        b.x+","+b.y+" "+b.width+"x"+b.height+")";
                break;
            case COMPONENT_RESIZED:
                typeStr="COMPONENT_RESIZED ("+
                        b.x+","+b.y+" "+b.width+"x"+b.height+")";
                break;
            default:
                typeStr="unknown type";
        }
        return typeStr;
    }
}
