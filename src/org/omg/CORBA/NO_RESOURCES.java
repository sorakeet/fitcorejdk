/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class NO_RESOURCES extends SystemException{
    public NO_RESOURCES(){
        this("");
    }

    public NO_RESOURCES(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public NO_RESOURCES(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public NO_RESOURCES(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
