/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class DUP2 extends StackInstruction implements PushInstruction{
    public DUP2(){
        super(com.sun.org.apache.bcel.internal.Constants.DUP2);
    }

    public void accept(Visitor v){
        v.visitStackProducer(this);
        v.visitPushInstruction(this);
        v.visitStackInstruction(this);
        v.visitDUP2(this);
    }
}
