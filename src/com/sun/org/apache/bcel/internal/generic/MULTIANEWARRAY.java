/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;
/** ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import com.sun.org.apache.bcel.internal.ExceptionConstants;
import com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import com.sun.org.apache.bcel.internal.util.ByteSequence;

import java.io.DataOutputStream;
import java.io.IOException;

public class MULTIANEWARRAY extends CPInstruction implements LoadClass, AllocationInstruction, ExceptionThrower{
    private short dimensions;

    MULTIANEWARRAY(){
    }

    public MULTIANEWARRAY(int index,short dimensions){
        super(com.sun.org.apache.bcel.internal.Constants.MULTIANEWARRAY,index);
        if(dimensions<1)
            throw new ClassGenException("Invalid dimensions value: "+dimensions);
        this.dimensions=dimensions;
        length=4;
    }

    public void dump(DataOutputStream out) throws IOException{
        out.writeByte(opcode);
        out.writeShort(index);
        out.writeByte(dimensions);
    }

    public String toString(boolean verbose){
        return super.toString(verbose)+" "+index+" "+dimensions;
    }

    public String toString(ConstantPool cp){
        return super.toString(cp)+" "+dimensions;
    }

    protected void initFromFile(ByteSequence bytes,boolean wide)
            throws IOException{
        super.initFromFile(bytes,wide);
        dimensions=bytes.readByte();
        length=4;
    }

    public final short getDimensions(){
        return dimensions;
    }

    public int consumeStack(ConstantPoolGen cpg){
        return dimensions;
    }

    public void accept(Visitor v){
        v.visitLoadClass(this);
        v.visitAllocationInstruction(this);
        v.visitExceptionThrower(this);
        v.visitTypedInstruction(this);
        v.visitCPInstruction(this);
        v.visitMULTIANEWARRAY(this);
    }

    public Class[] getExceptions(){
        Class[] cs=new Class[2+ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length];
        System.arraycopy(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION,0,
                cs,0,ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length);
        cs[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length+1]=ExceptionConstants.NEGATIVE_ARRAY_SIZE_EXCEPTION;
        cs[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length]=ExceptionConstants.ILLEGAL_ACCESS_ERROR;
        return cs;
    }

    public ObjectType getLoadClassType(ConstantPoolGen cpg){
        Type t=getType(cpg);
        if(t instanceof ArrayType){
            t=((ArrayType)t).getBasicType();
        }
        return (t instanceof ObjectType)?(ObjectType)t:null;
    }
}
