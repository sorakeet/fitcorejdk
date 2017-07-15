/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.awt.*;

public class ContainerEvent extends ComponentEvent{
    public static final int CONTAINER_FIRST=300;
    public static final int CONTAINER_LAST=301;
    public static final int COMPONENT_ADDED=CONTAINER_FIRST;
    public static final int COMPONENT_REMOVED=1+CONTAINER_FIRST;
    private static final long serialVersionUID=-4114942250539772041L;
    Component child;

    public ContainerEvent(Component source,int id,Component child){
        super(source,id);
        this.child=child;
    }

    public Container getContainer(){
        return (source instanceof Container)?(Container)source:null;
    }

    public Component getChild(){
        return child;
    }

    public String paramString(){
        String typeStr;
        switch(id){
            case COMPONENT_ADDED:
                typeStr="COMPONENT_ADDED";
                break;
            case COMPONENT_REMOVED:
                typeStr="COMPONENT_REMOVED";
                break;
            default:
                typeStr="unknown type";
        }
        return typeStr+",child="+child.getName();
    }
}
