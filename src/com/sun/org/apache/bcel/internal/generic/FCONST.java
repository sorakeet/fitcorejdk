/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class FCONST extends Instruction
        implements ConstantPushInstruction, TypedInstruction{
    private float value;

    FCONST(){
    }

    public FCONST(float f){
        super(com.sun.org.apache.bcel.internal.Constants.FCONST_0,(short)1);
        if(f==0.0)
            opcode=com.sun.org.apache.bcel.internal.Constants.FCONST_0;
        else if(f==1.0)
            opcode=com.sun.org.apache.bcel.internal.Constants.FCONST_1;
        else if(f==2.0)
            opcode=com.sun.org.apache.bcel.internal.Constants.FCONST_2;
        else
            throw new ClassGenException("FCONST can be used only for 0.0, 1.0 and 2.0: "+f);
        value=f;
    }

    public Number getValue(){
        return new Float(value);
    }

    public Type getType(ConstantPoolGen cp){
        return Type.FLOAT;
    }

    public void accept(Visitor v){
        v.visitPushInstruction(this);
        v.visitStackProducer(this);
        v.visitTypedInstruction(this);
        v.visitConstantPushInstruction(this);
        v.visitFCONST(this);
    }
}
