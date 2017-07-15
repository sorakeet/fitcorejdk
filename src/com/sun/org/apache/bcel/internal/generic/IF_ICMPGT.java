/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class IF_ICMPGT extends IfInstruction{
    IF_ICMPGT(){
    }

    public IF_ICMPGT(InstructionHandle target){
        super(com.sun.org.apache.bcel.internal.Constants.IF_ICMPGT,target);
    }

    public IfInstruction negate(){
        return new IF_ICMPLE(target);
    }

    public void accept(Visitor v){
        v.visitStackConsumer(this);
        v.visitBranchInstruction(this);
        v.visitIfInstruction(this);
        v.visitIF_ICMPGT(this);
    }
}
