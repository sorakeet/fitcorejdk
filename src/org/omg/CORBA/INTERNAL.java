/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class INTERNAL extends SystemException{
    public INTERNAL(){
        this("");
    }

    public INTERNAL(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public INTERNAL(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public INTERNAL(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
