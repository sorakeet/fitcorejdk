/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class BREAKPOINT extends Instruction{
    public BREAKPOINT(){
        super(com.sun.org.apache.bcel.internal.Constants.BREAKPOINT,(short)1);
    }

    public void accept(Visitor v){
        v.visitBREAKPOINT(this);
    }
}
