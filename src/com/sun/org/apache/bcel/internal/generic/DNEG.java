/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class DNEG extends ArithmeticInstruction{
    public DNEG(){
        super(com.sun.org.apache.bcel.internal.Constants.DNEG);
    }

    public void accept(Visitor v){
        v.visitTypedInstruction(this);
        v.visitStackProducer(this);
        v.visitStackConsumer(this);
        v.visitArithmeticInstruction(this);
        v.visitDNEG(this);
    }
}
