/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class LLOAD extends LoadInstruction{
    LLOAD(){
        super(com.sun.org.apache.bcel.internal.Constants.LLOAD,com.sun.org.apache.bcel.internal.Constants.LLOAD_0);
    }

    public LLOAD(int n){
        super(com.sun.org.apache.bcel.internal.Constants.LLOAD,com.sun.org.apache.bcel.internal.Constants.LLOAD_0,n);
    }

    public void accept(Visitor v){
        super.accept(v);
        v.visitLLOAD(this);
    }
}
