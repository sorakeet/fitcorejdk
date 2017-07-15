/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class INTF_REPOS extends SystemException{
    public INTF_REPOS(){
        this("");
    }

    public INTF_REPOS(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public INTF_REPOS(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public INTF_REPOS(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
