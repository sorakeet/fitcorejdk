/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class IFGT extends IfInstruction{
    IFGT(){
    }

    public IFGT(InstructionHandle target){
        super(com.sun.org.apache.bcel.internal.Constants.IFGT,target);
    }

    public IfInstruction negate(){
        return new IFLE(target);
    }

    public void accept(Visitor v){
        v.visitStackConsumer(this);
        v.visitBranchInstruction(this);
        v.visitIfInstruction(this);
        v.visitIFGT(this);
    }
}
