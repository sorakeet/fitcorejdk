/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class ILOAD extends LoadInstruction{
    ILOAD(){
        super(com.sun.org.apache.bcel.internal.Constants.ILOAD,com.sun.org.apache.bcel.internal.Constants.ILOAD_0);
    }

    public ILOAD(int n){
        super(com.sun.org.apache.bcel.internal.Constants.ILOAD,com.sun.org.apache.bcel.internal.Constants.ILOAD_0,n);
    }

    public void accept(Visitor v){
        super.accept(v);
        v.visitILOAD(this);
    }
}
