/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class MARSHAL extends SystemException{
    public MARSHAL(){
        this("");
    }

    public MARSHAL(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public MARSHAL(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public MARSHAL(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
