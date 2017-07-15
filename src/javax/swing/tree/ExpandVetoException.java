/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.tree;

import javax.swing.event.TreeExpansionEvent;

public class ExpandVetoException extends Exception{
    protected TreeExpansionEvent event;

    public ExpandVetoException(TreeExpansionEvent event){
        this(event,null);
    }

    public ExpandVetoException(TreeExpansionEvent event,String message){
        super(message);
        this.event=event;
    }
}
