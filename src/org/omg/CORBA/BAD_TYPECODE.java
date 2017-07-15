/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class BAD_TYPECODE extends SystemException{
    public BAD_TYPECODE(){
        this("");
    }

    public BAD_TYPECODE(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public BAD_TYPECODE(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public BAD_TYPECODE(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
