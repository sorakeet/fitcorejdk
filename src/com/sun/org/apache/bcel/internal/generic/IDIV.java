/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class IDIV extends ArithmeticInstruction implements ExceptionThrower{
    public IDIV(){
        super(com.sun.org.apache.bcel.internal.Constants.IDIV);
    }

    public Class[] getExceptions(){
        return new Class[]{com.sun.org.apache.bcel.internal.ExceptionConstants.ARITHMETIC_EXCEPTION};
    }

    public void accept(Visitor v){
        v.visitExceptionThrower(this);
        v.visitTypedInstruction(this);
        v.visitStackProducer(this);
        v.visitStackConsumer(this);
        v.visitArithmeticInstruction(this);
        v.visitIDIV(this);
    }
}
