/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class INITIALIZE extends SystemException{
    public INITIALIZE(){
        this("");
    }

    public INITIALIZE(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public INITIALIZE(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public INITIALIZE(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
