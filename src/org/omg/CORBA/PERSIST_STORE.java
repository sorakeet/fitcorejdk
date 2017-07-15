/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class PERSIST_STORE extends SystemException{
    public PERSIST_STORE(){
        this("");
    }

    public PERSIST_STORE(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public PERSIST_STORE(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public PERSIST_STORE(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
