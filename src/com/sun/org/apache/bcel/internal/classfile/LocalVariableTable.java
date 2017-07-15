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

public class LocalVariableTable extends Attribute{
    private int local_variable_table_length; // Table of local
    private LocalVariable[] local_variable_table;        // variables

    public LocalVariableTable(LocalVariableTable c){
        this(c.getNameIndex(),c.getLength(),c.getLocalVariableTable(),
                c.getConstantPool());
    }

    public LocalVariableTable(int name_index,int length,
                              LocalVariable[] local_variable_table,
                              ConstantPool constant_pool){
        super(Constants.ATTR_LOCAL_VARIABLE_TABLE,name_index,length,constant_pool);
        setLocalVariableTable(local_variable_table);
    }

    LocalVariableTable(int name_index,int length,DataInputStream file,
                       ConstantPool constant_pool) throws IOException{
        this(name_index,length,(LocalVariable[])null,constant_pool);
        local_variable_table_length=(file.readUnsignedShort());
        local_variable_table=new LocalVariable[local_variable_table_length];
        for(int i=0;i<local_variable_table_length;i++)
            local_variable_table[i]=new LocalVariable(file,constant_pool);
    }

    public void accept(Visitor v){
        v.visitLocalVariableTable(this);
    }

    public final void dump(DataOutputStream file) throws IOException{
        super.dump(file);
        file.writeShort(local_variable_table_length);
        for(int i=0;i<local_variable_table_length;i++)
            local_variable_table[i].dump(file);
    }

    public Attribute copy(ConstantPool constant_pool){
        LocalVariableTable c=(LocalVariableTable)clone();
        c.local_variable_table=new LocalVariable[local_variable_table_length];
        for(int i=0;i<local_variable_table_length;i++)
            c.local_variable_table[i]=local_variable_table[i].copy();
        c.constant_pool=constant_pool;
        return c;
    }

    public final String toString(){
        StringBuffer buf=new StringBuffer("");
        for(int i=0;i<local_variable_table_length;i++){
            buf.append(local_variable_table[i].toString());
            if(i<local_variable_table_length-1)
                buf.append('\n');
        }
        return buf.toString();
    }

    public final LocalVariable[] getLocalVariableTable(){
        return local_variable_table;
    }

    public final void setLocalVariableTable(LocalVariable[] local_variable_table){
        this.local_variable_table=local_variable_table;
        local_variable_table_length=(local_variable_table==null)?0:
                local_variable_table.length;
    }

    public final LocalVariable getLocalVariable(int index){
        for(int i=0;i<local_variable_table_length;i++)
            if(local_variable_table[i].getIndex()==index)
                return local_variable_table[i];
        return null;
    }

    public final int getTableLength(){
        return local_variable_table_length;
    }
}
