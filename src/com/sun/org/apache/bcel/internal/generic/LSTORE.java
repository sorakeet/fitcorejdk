/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class LSTORE extends StoreInstruction{
    LSTORE(){
        super(com.sun.org.apache.bcel.internal.Constants.LSTORE,com.sun.org.apache.bcel.internal.Constants.LSTORE_0);
    }

    public LSTORE(int n){
        super(com.sun.org.apache.bcel.internal.Constants.LSTORE,com.sun.org.apache.bcel.internal.Constants.LSTORE_0,n);
    }

    public void accept(Visitor v){
        super.accept(v);
        v.visitLSTORE(this);
    }
}
