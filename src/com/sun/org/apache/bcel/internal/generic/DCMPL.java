/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class DCMPL extends Instruction
        implements TypedInstruction, StackProducer, StackConsumer{
    public DCMPL(){
        super(com.sun.org.apache.bcel.internal.Constants.DCMPL,(short)1);
    }

    public Type getType(ConstantPoolGen cp){
        return Type.DOUBLE;
    }

    public void accept(Visitor v){
        v.visitTypedInstruction(this);
        v.visitStackProducer(this);
        v.visitStackConsumer(this);
        v.visitDCMPL(this);
    }
}
