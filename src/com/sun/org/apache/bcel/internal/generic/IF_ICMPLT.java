/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class IF_ICMPLT extends IfInstruction{
    IF_ICMPLT(){
    }

    public IF_ICMPLT(InstructionHandle target){
        super(com.sun.org.apache.bcel.internal.Constants.IF_ICMPLT,target);
    }

    public IfInstruction negate(){
        return new IF_ICMPGE(target);
    }

    public void accept(Visitor v){
        v.visitStackConsumer(this);
        v.visitBranchInstruction(this);
        v.visitIfInstruction(this);
        v.visitIF_ICMPLT(this);
    }
}
