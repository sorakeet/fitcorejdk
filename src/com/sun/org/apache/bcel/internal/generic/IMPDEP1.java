/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class IMPDEP1 extends Instruction{
    public IMPDEP1(){
        super(com.sun.org.apache.bcel.internal.Constants.IMPDEP1,(short)1);
    }

    public void accept(Visitor v){
        v.visitIMPDEP1(this);
    }
}
