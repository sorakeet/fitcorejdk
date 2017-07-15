/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class FSTORE extends StoreInstruction{
    FSTORE(){
        super(com.sun.org.apache.bcel.internal.Constants.FSTORE,com.sun.org.apache.bcel.internal.Constants.FSTORE_0);
    }

    public FSTORE(int n){
        super(com.sun.org.apache.bcel.internal.Constants.FSTORE,com.sun.org.apache.bcel.internal.Constants.FSTORE_0,n);
    }

    public void accept(Visitor v){
        super.accept(v);
        v.visitFSTORE(this);
    }
}
