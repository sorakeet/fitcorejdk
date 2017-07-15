/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public abstract class StackInstruction extends Instruction{
    StackInstruction(){
    }

    protected StackInstruction(short opcode){
        super(opcode,(short)1);
    }

    public Type getType(ConstantPoolGen cp){
        return Type.UNKNOWN;
    }
}
