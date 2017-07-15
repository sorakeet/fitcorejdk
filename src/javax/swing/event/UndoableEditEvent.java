/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import javax.swing.undo.UndoableEdit;

public class UndoableEditEvent extends java.util.EventObject{
    private UndoableEdit myEdit;

    public UndoableEditEvent(Object source,UndoableEdit edit){
        super(source);
        myEdit=edit;
    }

    public UndoableEdit getEdit(){
        return myEdit;
    }
}
