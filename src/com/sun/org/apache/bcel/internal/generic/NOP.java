/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class NOP extends Instruction{
    public NOP(){
        super(com.sun.org.apache.bcel.internal.Constants.NOP,(short)1);
    }

    public void accept(Visitor v){
        v.visitNOP(this);
    }
}
