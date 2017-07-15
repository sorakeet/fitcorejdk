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

import com.sun.org.apache.bcel.internal.Constants;

public final class ArrayType extends ReferenceType{
    private int dimensions;
    private Type basic_type;

    public ArrayType(byte type,int dimensions){
        this(BasicType.getType(type),dimensions);
    }

    public ArrayType(Type type,int dimensions){
        super(Constants.T_ARRAY,"<dummy>");
        if((dimensions<1)||(dimensions>Constants.MAX_BYTE))
            throw new ClassGenException("Invalid number of dimensions: "+dimensions);
        switch(type.getType()){
            case Constants.T_ARRAY:
                ArrayType array=(ArrayType)type;
                this.dimensions=dimensions+array.dimensions;
                basic_type=array.basic_type;
                break;
            case Constants.T_VOID:
                throw new ClassGenException("Invalid type: void[]");
            default: // Basic type or reference
                this.dimensions=dimensions;
                basic_type=type;
                break;
        }
        StringBuffer buf=new StringBuffer();
        for(int i=0;i<this.dimensions;i++)
            buf.append('[');
        buf.append(basic_type.getSignature());
        signature=buf.toString();
    }

    public ArrayType(String class_name,int dimensions){
        this(new ObjectType(class_name),dimensions);
    }

    public Type getBasicType(){
        return basic_type;
    }

    public Type getElementType(){
        if(dimensions==1)
            return basic_type;
        else
            return new ArrayType(basic_type,dimensions-1);
    }

    public int getDimensions(){
        return dimensions;
    }

    public int hashCode(){
        return basic_type.hashCode()^dimensions;
    }

    public boolean equals(Object type){
        if(type instanceof ArrayType){
            ArrayType array=(ArrayType)type;
            return (array.dimensions==dimensions)&&array.basic_type.equals(basic_type);
        }else
            return false;
    }
}
