/**
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd;

public class DropTargetEvent extends java.util.EventObject{
    private static final long serialVersionUID=2821229066521922993L;
    protected DropTargetContext context;

    public DropTargetEvent(DropTargetContext dtc){
        super(dtc.getDropTarget());
        context=dtc;
    }

    public DropTargetContext getDropTargetContext(){
        return context;
    }
}
