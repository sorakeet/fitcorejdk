/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class ARETURN extends ReturnInstruction{
    public ARETURN(){
        super(com.sun.org.apache.bcel.internal.Constants.ARETURN);
    }

    public void accept(Visitor v){
        v.visitExceptionThrower(this);
        v.visitTypedInstruction(this);
        v.visitStackConsumer(this);
        v.visitReturnInstruction(this);
        v.visitARETURN(this);
    }
}
