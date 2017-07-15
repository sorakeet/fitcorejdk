/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.classfile;
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
import com.sun.org.apache.bcel.internal.generic.Type;

import java.io.DataInputStream;
import java.io.IOException;

public final class Field extends FieldOrMethod{
    public Field(Field c){
        super(c);
    }

    Field(DataInputStream file,ConstantPool constant_pool)
            throws IOException, ClassFormatException{
        super(file,constant_pool);
    }

    public Field(int access_flags,int name_index,int signature_index,
                 Attribute[] attributes,ConstantPool constant_pool){
        super(access_flags,name_index,signature_index,attributes,constant_pool);
    }

    public void accept(Visitor v){
        v.visitField(this);
    }

    public final String toString(){
        String name, signature, access; // Short cuts to constant pool
        // Get names from constant pool
        access=Utility.accessToString(access_flags);
        access=access.equals("")?"":(access+" ");
        signature=Utility.signatureToString(getSignature());
        name=getName();
        StringBuffer buf=new StringBuffer(access+signature+" "+name);
        ConstantValue cv=getConstantValue();
        if(cv!=null)
            buf.append(" = "+cv);
        for(int i=0;i<attributes_count;i++){
            Attribute a=attributes[i];
            if(!(a instanceof ConstantValue))
                buf.append(" ["+a.toString()+"]");
        }
        return buf.toString();
    }

    public final ConstantValue getConstantValue(){
        for(int i=0;i<attributes_count;i++)
            if(attributes[i].getTag()==Constants.ATTR_CONSTANT_VALUE)
                return (ConstantValue)attributes[i];
        return null;
    }

    public final Field copy(ConstantPool constant_pool){
        return (Field)copy_(constant_pool);
    }

    public Type getType(){
        return Type.getReturnType(getSignature());
    }
}
