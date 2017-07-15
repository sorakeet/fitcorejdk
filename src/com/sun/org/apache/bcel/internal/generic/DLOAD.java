/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class DLOAD extends LoadInstruction{
    DLOAD(){
        super(com.sun.org.apache.bcel.internal.Constants.DLOAD,com.sun.org.apache.bcel.internal.Constants.DLOAD_0);
    }

    public DLOAD(int n){
        super(com.sun.org.apache.bcel.internal.Constants.DLOAD,com.sun.org.apache.bcel.internal.Constants.DLOAD_0,n);
    }

    public void accept(Visitor v){
        super.accept(v);
        v.visitDLOAD(this);
    }
}
