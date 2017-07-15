/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class ICONST extends Instruction
        implements ConstantPushInstruction, TypedInstruction{
    private int value;

    ICONST(){
    }

    public ICONST(int i){
        super(com.sun.org.apache.bcel.internal.Constants.ICONST_0,(short)1);
        if((i>=-1)&&(i<=5))
            opcode=(short)(com.sun.org.apache.bcel.internal.Constants.ICONST_0+i); // Even works for i == -1
        else
            throw new ClassGenException("ICONST can be used only for value between -1 and 5: "+
                    i);
        value=i;
    }

    public Number getValue(){
        return new Integer(value);
    }

    public Type getType(ConstantPoolGen cp){
        return Type.INT;
    }

    public void accept(Visitor v){
        v.visitPushInstruction(this);
        v.visitStackProducer(this);
        v.visitTypedInstruction(this);
        v.visitConstantPushInstruction(this);
        v.visitICONST(this);
    }
}
