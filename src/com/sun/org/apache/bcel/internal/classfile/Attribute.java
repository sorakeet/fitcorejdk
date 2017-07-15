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
import java.io.Serializable;
import java.util.HashMap;

public abstract class Attribute implements Cloneable, Node, Serializable{
    private static HashMap readers=new HashMap();
    protected int name_index; // Points to attribute name in constant pool
    protected int length;     // Content length of attribute field
    protected byte tag;        // Tag to distiguish subclasses
    protected ConstantPool constant_pool;

    protected Attribute(byte tag,int name_index,int length,
                        ConstantPool constant_pool){
        this.tag=tag;
        this.name_index=name_index;
        this.length=length;
        this.constant_pool=constant_pool;
    }

    public static void addAttributeReader(String name,AttributeReader r){
        readers.put(name,r);
    }

    public static void removeAttributeReader(String name){
        readers.remove(name);
    }

    public static final Attribute readAttribute(DataInputStream file,
                                                ConstantPool constant_pool)
            throws IOException, ClassFormatException{
        ConstantUtf8 c;
        String name;
        int name_index;
        int length;
        byte tag=Constants.ATTR_UNKNOWN; // Unknown attribute
        // Get class name from constant pool via `name_index' indirection
        name_index=(int)file.readUnsignedShort();
        c=(ConstantUtf8)constant_pool.getConstant(name_index,
                Constants.CONSTANT_Utf8);
        name=c.getBytes();
        // Length of data in bytes
        length=file.readInt();
        // Compare strings to find known attribute
        for(byte i=0;i<Constants.KNOWN_ATTRIBUTES;i++){
            if(name.equals(Constants.ATTRIBUTE_NAMES[i])){
                tag=i; // found!
                break;
            }
        }
        // Call proper constructor, depending on `tag'
        switch(tag){
            case Constants.ATTR_UNKNOWN:
                AttributeReader r=(AttributeReader)readers.get(name);
                if(r!=null)
                    return r.createAttribute(name_index,length,file,constant_pool);
                else
                    return new Unknown(name_index,length,file,constant_pool);
            case Constants.ATTR_CONSTANT_VALUE:
                return new ConstantValue(name_index,length,file,constant_pool);
            case Constants.ATTR_SOURCE_FILE:
                return new SourceFile(name_index,length,file,constant_pool);
            case Constants.ATTR_CODE:
                return new Code(name_index,length,file,constant_pool);
            case Constants.ATTR_EXCEPTIONS:
                return new ExceptionTable(name_index,length,file,constant_pool);
            case Constants.ATTR_LINE_NUMBER_TABLE:
                return new LineNumberTable(name_index,length,file,constant_pool);
            case Constants.ATTR_LOCAL_VARIABLE_TABLE:
                return new LocalVariableTable(name_index,length,file,constant_pool);
            case Constants.ATTR_LOCAL_VARIABLE_TYPE_TABLE:
                return new LocalVariableTypeTable(name_index,length,file,constant_pool);
            case Constants.ATTR_INNER_CLASSES:
                return new InnerClasses(name_index,length,file,constant_pool);
            case Constants.ATTR_SYNTHETIC:
                return new Synthetic(name_index,length,file,constant_pool);
            case Constants.ATTR_DEPRECATED:
                return new Deprecated(name_index,length,file,constant_pool);
            case Constants.ATTR_PMG:
                return new PMGClass(name_index,length,file,constant_pool);
            case Constants.ATTR_SIGNATURE:
                return new Signature(name_index,length,file,constant_pool);
            case Constants.ATTR_STACK_MAP:
                return new StackMap(name_index,length,file,constant_pool);
            default: // Never reached
                throw new IllegalStateException("Ooops! default case reached.");
        }
    }

    public abstract void accept(Visitor v);

    public void dump(DataOutputStream file) throws IOException{
        file.writeShort(name_index);
        file.writeInt(length);
    }

    public final int getLength(){
        return length;
    }

    public final void setLength(int length){
        this.length=length;
    }

    public final int getNameIndex(){
        return name_index;
    }

    public final void setNameIndex(int name_index){
        this.name_index=name_index;
    }

    public final byte getTag(){
        return tag;
    }

    public final ConstantPool getConstantPool(){
        return constant_pool;
    }

    public final void setConstantPool(ConstantPool constant_pool){
        this.constant_pool=constant_pool;
    }

    public Object clone(){
        Object o=null;
        try{
            o=super.clone();
        }catch(CloneNotSupportedException e){
            e.printStackTrace(); // Never occurs
        }
        return o;
    }

    public String toString(){
        return Constants.ATTRIBUTE_NAMES[tag];
    }

    public abstract Attribute copy(ConstantPool constant_pool);
}
