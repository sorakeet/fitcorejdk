/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public class INSTANCEOF extends CPInstruction
        implements LoadClass, ExceptionThrower, StackProducer, StackConsumer{
    INSTANCEOF(){
    }

    public INSTANCEOF(int index){
        super(com.sun.org.apache.bcel.internal.Constants.INSTANCEOF,index);
    }

    public Class[] getExceptions(){
        return com.sun.org.apache.bcel.internal.ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION;
    }

    public ObjectType getLoadClassType(ConstantPoolGen cpg){
        Type t=getType(cpg);
        if(t instanceof ArrayType)
            t=((ArrayType)t).getBasicType();
        return (t instanceof ObjectType)?(ObjectType)t:null;
    }

    public void accept(Visitor v){
        v.visitLoadClass(this);
        v.visitExceptionThrower(this);
        v.visitStackProducer(this);
        v.visitStackConsumer(this);
        v.visitTypedInstruction(this);
        v.visitCPInstruction(this);
        v.visitINSTANCEOF(this);
    }
}
