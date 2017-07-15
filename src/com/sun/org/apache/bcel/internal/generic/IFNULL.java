/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class IFNULL extends IfInstruction{
    IFNULL(){
    }

    public IFNULL(InstructionHandle target){
        super(com.sun.org.apache.bcel.internal.Constants.IFNULL,target);
    }

    public IfInstruction negate(){
        return new IFNONNULL(target);
    }

    public void accept(Visitor v){
        v.visitStackConsumer(this);
        v.visitBranchInstruction(this);
        v.visitIfInstruction(this);
        v.visitIFNULL(this);
    }
}
