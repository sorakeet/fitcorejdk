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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ExceptionTable extends Attribute{
    private int number_of_exceptions;  // Table of indices into
    private int[] exception_index_table; // constant pool

    public ExceptionTable(ExceptionTable c){
        this(c.getNameIndex(),c.getLength(),c.getExceptionIndexTable(),
                c.getConstantPool());
    }

    public ExceptionTable(int name_index,int length,
                          int[] exception_index_table,
                          ConstantPool constant_pool){
        super(Constants.ATTR_EXCEPTIONS,name_index,length,constant_pool);
        setExceptionIndexTable(exception_index_table);
    }

    ExceptionTable(int name_index,int length,DataInputStream file,
                   ConstantPool constant_pool) throws IOException{
        this(name_index,length,(int[])null,constant_pool);
        number_of_exceptions=file.readUnsignedShort();
        exception_index_table=new int[number_of_exceptions];
        for(int i=0;i<number_of_exceptions;i++)
            exception_index_table[i]=file.readUnsignedShort();
    }

    public void accept(Visitor v){
        v.visitExceptionTable(this);
    }

    public final void dump(DataOutputStream file) throws IOException{
        super.dump(file);
        file.writeShort(number_of_exceptions);
        for(int i=0;i<number_of_exceptions;i++)
            file.writeShort(exception_index_table[i]);
    }

    public Attribute copy(ConstantPool constant_pool){
        ExceptionTable c=(ExceptionTable)clone();
        c.exception_index_table=(int[])exception_index_table.clone();
        c.constant_pool=constant_pool;
        return c;
    }

    public final String toString(){
        StringBuffer buf=new StringBuffer("");
        String str;
        for(int i=0;i<number_of_exceptions;i++){
            str=constant_pool.getConstantString(exception_index_table[i],
                    Constants.CONSTANT_Class);
            buf.append(Utility.compactClassName(str,false));
            if(i<number_of_exceptions-1)
                buf.append(", ");
        }
        return buf.toString();
    }

    public final int[] getExceptionIndexTable(){
        return exception_index_table;
    }

    public final void setExceptionIndexTable(int[] exception_index_table){
        this.exception_index_table=exception_index_table;
        number_of_exceptions=(exception_index_table==null)?0:
                exception_index_table.length;
    }

    public final int getNumberOfExceptions(){
        return number_of_exceptions;
    }

    public final String[] getExceptionNames(){
        String[] names=new String[number_of_exceptions];
        for(int i=0;i<number_of_exceptions;i++)
            names[i]=constant_pool.getConstantString(exception_index_table[i],
                    Constants.CONSTANT_Class).
                    replace('/','.');
        return names;
    }
}
