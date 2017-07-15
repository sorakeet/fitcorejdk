/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class OBJ_ADAPTER extends SystemException{
    public OBJ_ADAPTER(){
        this("");
    }

    public OBJ_ADAPTER(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public OBJ_ADAPTER(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public OBJ_ADAPTER(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
