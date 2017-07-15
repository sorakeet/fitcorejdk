/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public abstract class IfInstruction extends BranchInstruction implements StackConsumer{
    IfInstruction(){
    }

    protected IfInstruction(short opcode,InstructionHandle target){
        super(opcode,target);
    }

    public abstract IfInstruction negate();
}
