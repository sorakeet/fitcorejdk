/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class MONITOREXIT extends Instruction
        implements ExceptionThrower, StackConsumer{
    public MONITOREXIT(){
        super(com.sun.org.apache.bcel.internal.Constants.MONITOREXIT,(short)1);
    }

    public Class[] getExceptions(){
        return new Class[]{com.sun.org.apache.bcel.internal.ExceptionConstants.NULL_POINTER_EXCEPTION};
    }

    public void accept(Visitor v){
        v.visitExceptionThrower(this);
        v.visitStackConsumer(this);
        v.visitMONITOREXIT(this);
    }
}
