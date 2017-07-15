/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public abstract class JsrInstruction extends BranchInstruction
        implements UnconditionalBranch, TypedInstruction, StackProducer{
    JsrInstruction(short opcode,InstructionHandle target){
        super(opcode,target);
    }

    JsrInstruction(){
    }

    public Type getType(ConstantPoolGen cp){
        return new ReturnaddressType(physicalSuccessor());
    }

    public InstructionHandle physicalSuccessor(){
        InstructionHandle ih=this.target;
        // Rewind!
        while(ih.getPrev()!=null)
            ih=ih.getPrev();
        // Find the handle for "this" JsrInstruction object.
        while(ih.getInstruction()!=this)
            ih=ih.getNext();
        InstructionHandle toThis=ih;
        while(ih!=null){
            ih=ih.getNext();
            if((ih!=null)&&(ih.getInstruction()==this))
                throw new RuntimeException("physicalSuccessor() called on a shared JsrInstruction.");
        }
        // Return the physical successor
        return toThis.getNext();
    }
}
