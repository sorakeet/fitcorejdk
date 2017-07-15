/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class ASTORE extends StoreInstruction{
    ASTORE(){
        super(com.sun.org.apache.bcel.internal.Constants.ASTORE,com.sun.org.apache.bcel.internal.Constants.ASTORE_0);
    }

    public ASTORE(int n){
        super(com.sun.org.apache.bcel.internal.Constants.ASTORE,com.sun.org.apache.bcel.internal.Constants.ASTORE_0,n);
    }

    public void accept(Visitor v){
        super.accept(v);
        v.visitASTORE(this);
    }
}
