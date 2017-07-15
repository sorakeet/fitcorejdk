/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class LCONST extends Instruction
        implements ConstantPushInstruction, TypedInstruction{
    private long value;

    LCONST(){
    }

    public LCONST(long l){
        super(com.sun.org.apache.bcel.internal.Constants.LCONST_0,(short)1);
        if(l==0)
            opcode=com.sun.org.apache.bcel.internal.Constants.LCONST_0;
        else if(l==1)
            opcode=com.sun.org.apache.bcel.internal.Constants.LCONST_1;
        else
            throw new ClassGenException("LCONST can be used only for 0 and 1: "+l);
        value=l;
    }

    public Number getValue(){
        return new Long(value);
    }

    public Type getType(ConstantPoolGen cp){
        return Type.LONG;
    }

    public void accept(Visitor v){
        v.visitPushInstruction(this);
        v.visitStackProducer(this);
        v.visitTypedInstruction(this);
        v.visitConstantPushInstruction(this);
        v.visitLCONST(this);
    }
}
