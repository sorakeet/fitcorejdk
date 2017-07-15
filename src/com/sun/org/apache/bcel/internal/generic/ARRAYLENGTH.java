/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class ARRAYLENGTH extends Instruction
        implements ExceptionThrower, StackProducer{
    public ARRAYLENGTH(){
        super(com.sun.org.apache.bcel.internal.Constants.ARRAYLENGTH,(short)1);
    }

    public Class[] getExceptions(){
        return new Class[]{com.sun.org.apache.bcel.internal.ExceptionConstants.NULL_POINTER_EXCEPTION};
    }

    public void accept(Visitor v){
        v.visitExceptionThrower(this);
        v.visitStackProducer(this);
        v.visitARRAYLENGTH(this);
    }
}
