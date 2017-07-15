/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class COMM_FAILURE extends SystemException{
    public COMM_FAILURE(){
        this("");
    }

    public COMM_FAILURE(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public COMM_FAILURE(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public COMM_FAILURE(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
