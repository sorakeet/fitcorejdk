/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class INV_OBJREF extends SystemException{
    public INV_OBJREF(){
        this("");
    }

    public INV_OBJREF(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public INV_OBJREF(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public INV_OBJREF(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
