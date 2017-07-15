/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class SWAP extends StackInstruction implements StackConsumer, StackProducer{
    public SWAP(){
        super(com.sun.org.apache.bcel.internal.Constants.SWAP);
    }

    public void accept(Visitor v){
        v.visitStackConsumer(this);
        v.visitStackProducer(this);
        v.visitStackInstruction(this);
        v.visitSWAP(this);
    }
}
