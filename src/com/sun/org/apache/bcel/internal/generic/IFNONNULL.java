/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class IFNONNULL extends IfInstruction{
    IFNONNULL(){
    }

    public IFNONNULL(InstructionHandle target){
        super(com.sun.org.apache.bcel.internal.Constants.IFNONNULL,target);
    }

    public IfInstruction negate(){
        return new IFNULL(target);
    }

    public void accept(Visitor v){
        v.visitStackConsumer(this);
        v.visitBranchInstruction(this);
        v.visitIfInstruction(this);
        v.visitIFNONNULL(this);
    }
}
