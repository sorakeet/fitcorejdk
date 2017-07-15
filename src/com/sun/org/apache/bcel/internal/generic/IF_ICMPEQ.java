/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class IF_ICMPEQ extends IfInstruction{
    IF_ICMPEQ(){
    }

    public IF_ICMPEQ(InstructionHandle target){
        super(com.sun.org.apache.bcel.internal.Constants.IF_ICMPEQ,target);
    }

    public IfInstruction negate(){
        return new IF_ICMPNE(target);
    }

    public void accept(Visitor v){
        v.visitStackConsumer(this);
        v.visitBranchInstruction(this);
        v.visitIfInstruction(this);
        v.visitIF_ICMPEQ(this);
    }
}
