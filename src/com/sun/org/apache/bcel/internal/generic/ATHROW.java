/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class ATHROW extends Instruction implements UnconditionalBranch, ExceptionThrower{
    public ATHROW(){
        super(com.sun.org.apache.bcel.internal.Constants.ATHROW,(short)1);
    }

    public Class[] getExceptions(){
        return new Class[]{com.sun.org.apache.bcel.internal.ExceptionConstants.THROWABLE};
    }

    public void accept(Visitor v){
        v.visitUnconditionalBranch(this);
        v.visitExceptionThrower(this);
        v.visitATHROW(this);
    }
}
