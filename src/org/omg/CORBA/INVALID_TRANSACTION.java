/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class INVALID_TRANSACTION extends SystemException{
    public INVALID_TRANSACTION(){
        this("");
    }

    public INVALID_TRANSACTION(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public INVALID_TRANSACTION(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public INVALID_TRANSACTION(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
