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

public final class StackMap extends Attribute implements Node{
    private int map_length;
    private StackMapEntry[] map; // Table of stack map entries

    StackMap(int name_index,int length,DataInputStream file,
             ConstantPool constant_pool) throws IOException{
        this(name_index,length,(StackMapEntry[])null,constant_pool);
        map_length=file.readUnsignedShort();
        map=new StackMapEntry[map_length];
        for(int i=0;i<map_length;i++)
            map[i]=new StackMapEntry(file,constant_pool);
    }

    public StackMap(int name_index,int length,StackMapEntry[] map,
                    ConstantPool constant_pool){
        super(Constants.ATTR_STACK_MAP,name_index,length,constant_pool);
        setStackMap(map);
    }

    public final StackMapEntry[] getStackMap(){
        return map;
    }

    public final void setStackMap(StackMapEntry[] map){
        this.map=map;
        map_length=(map==null)?0:map.length;
    }

    public void accept(Visitor v){
        v.visitStackMap(this);
    }

    public final void dump(DataOutputStream file) throws IOException{
        super.dump(file);
        file.writeShort(map_length);
        for(int i=0;i<map_length;i++)
            map[i].dump(file);
    }

    public Attribute copy(ConstantPool constant_pool){
        StackMap c=(StackMap)clone();
        c.map=new StackMapEntry[map_length];
        for(int i=0;i<map_length;i++)
            c.map[i]=map[i].copy();
        c.constant_pool=constant_pool;
        return c;
    }

    public final String toString(){
        StringBuffer buf=new StringBuffer("StackMap(");
        for(int i=0;i<map_length;i++){
            buf.append(map[i].toString());
            if(i<map_length-1)
                buf.append(", ");
        }
        buf.append(')');
        return buf.toString();
    }

    public final int getMapLength(){
        return map_length;
    }
}
