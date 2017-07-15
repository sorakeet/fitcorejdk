/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class DUP_X2 extends StackInstruction{
    public DUP_X2(){
        super(com.sun.org.apache.bcel.internal.Constants.DUP_X2);
    }

    public void accept(Visitor v){
        v.visitStackInstruction(this);
        v.visitDUP_X2(this);
    }
}
