/**
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public final class IMP_LIMIT extends SystemException{
    public IMP_LIMIT(){
        this("");
    }

    public IMP_LIMIT(String s){
        this(s,0,CompletionStatus.COMPLETED_NO);
    }

    public IMP_LIMIT(String s,int minor,CompletionStatus completed){
        super(s,minor,completed);
    }

    public IMP_LIMIT(int minor,CompletionStatus completed){
        this("",minor,completed);
    }
}
