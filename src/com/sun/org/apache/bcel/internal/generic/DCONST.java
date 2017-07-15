/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class DCONST extends Instruction
        implements ConstantPushInstruction, TypedInstruction{
    private double value;

    DCONST(){
    }

    public DCONST(double f){
        super(com.sun.org.apache.bcel.internal.Constants.DCONST_0,(short)1);
        if(f==0.0)
            opcode=com.sun.org.apache.bcel.internal.Constants.DCONST_0;
        else if(f==1.0)
            opcode=com.sun.org.apache.bcel.internal.Constants.DCONST_1;
        else
            throw new ClassGenException("DCONST can be used only for 0.0 and 1.0: "+f);
        value=f;
    }

    public Number getValue(){
        return new Double(value);
    }

    public Type getType(ConstantPoolGen cp){
        return Type.DOUBLE;
    }

    public void accept(Visitor v){
        v.visitPushInstruction(this);
        v.visitStackProducer(this);
        v.visitTypedInstruction(this);
        v.visitConstantPushInstruction(this);
        v.visitDCONST(this);
    }
}
