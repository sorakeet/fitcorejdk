/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class AASTORE extends ArrayInstruction implements StackConsumer{
    public AASTORE(){
        super(com.sun.org.apache.bcel.internal.Constants.AASTORE);
    }

    public void accept(Visitor v){
        v.visitStackConsumer(this);
        v.visitExceptionThrower(this);
        v.visitTypedInstruction(this);
        v.visitArrayInstruction(this);
        v.visitAASTORE(this);
    }
}
