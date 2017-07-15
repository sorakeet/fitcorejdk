/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class IF_ICMPNE extends IfInstruction{
    IF_ICMPNE(){
    }

    public IF_ICMPNE(InstructionHandle target){
        super(com.sun.org.apache.bcel.internal.Constants.IF_ICMPNE,target);
    }

    public IfInstruction negate(){
        return new IF_ICMPEQ(target);
    }

    public void accept(Visitor v){
        v.visitStackConsumer(this);
        v.visitBranchInstruction(this);
        v.visitIfInstruction(this);
        v.visitIF_ICMPNE(this);
    }
}
