/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class TRANSACTION_REQUIRED extends SystemException{
    public TRANSACTION_REQUIRED(){
        this("");
    }

    public TRANSACTION_REQUIRED(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public TRANSACTION_REQUIRED(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public TRANSACTION_REQUIRED(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
