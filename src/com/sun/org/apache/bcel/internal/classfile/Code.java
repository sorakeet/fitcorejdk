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

public final class Code extends Attribute{
    private int max_stack;   // Maximum size of stack used by this method
    private int max_locals;  // Number of local variables
    private int code_length; // Length of code in bytes
    private byte[] code;        // Actual byte code
    private int exception_table_length;
    private CodeException[] exception_table;  // Table of handled exceptions
    private int attributes_count; // Attributes of code: LineNumber
    private Attribute[] attributes;       // or LocalVariable

    public Code(Code c){
        this(c.getNameIndex(),c.getLength(),c.getMaxStack(),c.getMaxLocals(),
                c.getCode(),c.getExceptionTable(),c.getAttributes(),
                c.getConstantPool());
    }

    public Code(int name_index,int length,
                int max_stack,int max_locals,
                byte[] code,
                CodeException[] exception_table,
                Attribute[] attributes,
                ConstantPool constant_pool){
        super(Constants.ATTR_CODE,name_index,length,constant_pool);
        this.max_stack=max_stack;
        this.max_locals=max_locals;
        setCode(code);
        setExceptionTable(exception_table);
        setAttributes(attributes); // Overwrites length!
    }

    Code(int name_index,int length,DataInputStream file,
         ConstantPool constant_pool) throws IOException{
        // Initialize with some default values which will be overwritten later
        this(name_index,length,
                file.readUnsignedShort(),file.readUnsignedShort(),
                (byte[])null,(CodeException[])null,(Attribute[])null,
                constant_pool);
        code_length=file.readInt();
        code=new byte[code_length]; // Read byte code
        file.readFully(code);
        /** Read exception table that contains all regions where an exception
         * handler is active, i.e., a try { ... } catch() block.
         */
        exception_table_length=file.readUnsignedShort();
        exception_table=new CodeException[exception_table_length];
        for(int i=0;i<exception_table_length;i++)
            exception_table[i]=new CodeException(file);
        /** Read all attributes, currently `LineNumberTable' and
         * `LocalVariableTable'
         */
        attributes_count=file.readUnsignedShort();
        attributes=new Attribute[attributes_count];
        for(int i=0;i<attributes_count;i++)
            attributes[i]=Attribute.readAttribute(file,constant_pool);
        /** Adjust length, because of setAttributes in this(), s.b.  length
         * is incorrect, because it didn't take the internal attributes
         * into account yet! Very subtle bug, fixed in 3.1.1.
         */
        this.length=length;
    }

    public void accept(Visitor v){
        v.visitCode(this);
    }

    public final void dump(DataOutputStream file) throws IOException{
        super.dump(file);
        file.writeShort(max_stack);
        file.writeShort(max_locals);
        file.writeInt(code_length);
        file.write(code,0,code_length);
        file.writeShort(exception_table_length);
        for(int i=0;i<exception_table_length;i++)
            exception_table[i].dump(file);
        file.writeShort(attributes_count);
        for(int i=0;i<attributes_count;i++)
            attributes[i].dump(file);
    }

    public Attribute copy(ConstantPool constant_pool){
        Code c=(Code)clone();
        c.code=(byte[])code.clone();
        c.constant_pool=constant_pool;
        c.exception_table=new CodeException[exception_table_length];
        for(int i=0;i<exception_table_length;i++)
            c.exception_table[i]=exception_table[i].copy();
        c.attributes=new Attribute[attributes_count];
        for(int i=0;i<attributes_count;i++)
            c.attributes[i]=attributes[i].copy(constant_pool);
        return c;
    }

    public final String toString(){
        return toString(true);
    }

    public final String toString(boolean verbose){
        StringBuffer buf;
        buf=new StringBuffer("Code(max_stack = "+max_stack+
                ", max_locals = "+max_locals+
                ", code_length = "+code_length+")\n"+
                Utility.codeToString(code,constant_pool,0,-1,verbose));
        if(exception_table_length>0){
            buf.append("\nException handler(s) = \n"+"From\tTo\tHandler\tType\n");
            for(int i=0;i<exception_table_length;i++)
                buf.append(exception_table[i].toString(constant_pool,verbose)+"\n");
        }
        if(attributes_count>0){
            buf.append("\nAttribute(s) = \n");
            for(int i=0;i<attributes_count;i++)
                buf.append(attributes[i].toString()+"\n");
        }
        return buf.toString();
    }

    public final Attribute[] getAttributes(){
        return attributes;
    }

    public final void setAttributes(Attribute[] attributes){
        this.attributes=attributes;
        attributes_count=(attributes==null)?0:attributes.length;
        length=calculateLength(); // Adjust length
    }

    private final int calculateLength(){
        int len=0;
        for(int i=0;i<attributes_count;i++)
            len+=attributes[i].length+6 /**attribute header size*/;
        return len+getInternalLength();
    }

    private final int getInternalLength(){
        return 2 /**max_stack*/+2 /**max_locals*/+4 /**code length*/
                +code_length /**byte-code*/
                +2 /**exception-table length*/
                +8*exception_table_length /** exception table */
                +2 /** attributes count */;
    }

    public LineNumberTable getLineNumberTable(){
        for(int i=0;i<attributes_count;i++)
            if(attributes[i] instanceof LineNumberTable)
                return (LineNumberTable)attributes[i];
        return null;
    }

    public LocalVariableTable getLocalVariableTable(){
        for(int i=0;i<attributes_count;i++)
            if(attributes[i] instanceof LocalVariableTable)
                return (LocalVariableTable)attributes[i];
        return null;
    }

    public final byte[] getCode(){
        return code;
    }

    public final void setCode(byte[] code){
        this.code=code;
        code_length=(code==null)?0:code.length;
    }

    public final CodeException[] getExceptionTable(){
        return exception_table;
    }

    public final void setExceptionTable(CodeException[] exception_table){
        this.exception_table=exception_table;
        exception_table_length=(exception_table==null)?0:
                exception_table.length;
    }

    public final int getMaxLocals(){
        return max_locals;
    }

    public final void setMaxLocals(int max_locals){
        this.max_locals=max_locals;
    }

    public final int getMaxStack(){
        return max_stack;
    }

    public final void setMaxStack(int max_stack){
        this.max_stack=max_stack;
    }
}
