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

public class ConstantPool implements Cloneable, Node, Serializable{
    private int constant_pool_count;
    private Constant[] constant_pool;

    public ConstantPool(Constant[] constant_pool){
        setConstantPool(constant_pool);
    }

    ConstantPool(DataInputStream file) throws IOException, ClassFormatException{
        byte tag;
        constant_pool_count=file.readUnsignedShort();
        constant_pool=new Constant[constant_pool_count];
        /** constant_pool[0] is unused by the compiler and may be used freely
         * by the implementation.
         */
        for(int i=1;i<constant_pool_count;i++){
            constant_pool[i]=Constant.readConstant(file);
            /** Quote from the JVM specification:
             * "All eight byte constants take up two spots in the constant pool.
             * If this is the n'th byte in the constant pool, then the next item
             * will be numbered n+2"
             *
             * Thus we have to increment the index counter.
             */
            tag=constant_pool[i].getTag();
            if((tag==Constants.CONSTANT_Double)||(tag==Constants.CONSTANT_Long))
                i++;
        }
    }

    private static final String escape(String str){
        int len=str.length();
        StringBuffer buf=new StringBuffer(len+5);
        char[] ch=str.toCharArray();
        for(int i=0;i<len;i++){
            switch(ch[i]){
                case '\n':
                    buf.append("\\n");
                    break;
                case '\r':
                    buf.append("\\r");
                    break;
                case '\t':
                    buf.append("\\t");
                    break;
                case '\b':
                    buf.append("\\b");
                    break;
                case '"':
                    buf.append("\\\"");
                    break;
                default:
                    buf.append(ch[i]);
            }
        }
        return buf.toString();
    }

    public void accept(Visitor v){
        v.visitConstantPool(this);
    }

    public String constantToString(Constant c)
            throws ClassFormatException{
        String str;
        int i;
        byte tag=c.getTag();
        switch(tag){
            case Constants.CONSTANT_Class:
                i=((ConstantClass)c).getNameIndex();
                c=getConstant(i,Constants.CONSTANT_Utf8);
                str=Utility.compactClassName(((ConstantUtf8)c).getBytes(),false);
                break;
            case Constants.CONSTANT_String:
                i=((ConstantString)c).getStringIndex();
                c=getConstant(i,Constants.CONSTANT_Utf8);
                str="\""+escape(((ConstantUtf8)c).getBytes())+"\"";
                break;
            case Constants.CONSTANT_Utf8:
                str=((ConstantUtf8)c).getBytes();
                break;
            case Constants.CONSTANT_Double:
                str=""+((ConstantDouble)c).getBytes();
                break;
            case Constants.CONSTANT_Float:
                str=""+((ConstantFloat)c).getBytes();
                break;
            case Constants.CONSTANT_Long:
                str=""+((ConstantLong)c).getBytes();
                break;
            case Constants.CONSTANT_Integer:
                str=""+((ConstantInteger)c).getBytes();
                break;
            case Constants.CONSTANT_NameAndType:
                str=(constantToString(((ConstantNameAndType)c).getNameIndex(),
                        Constants.CONSTANT_Utf8)+" "+
                        constantToString(((ConstantNameAndType)c).getSignatureIndex(),
                                Constants.CONSTANT_Utf8));
                break;
            case Constants.CONSTANT_InterfaceMethodref:
            case Constants.CONSTANT_Methodref:
            case Constants.CONSTANT_Fieldref:
                str=(constantToString(((ConstantCP)c).getClassIndex(),
                        Constants.CONSTANT_Class)+"."+
                        constantToString(((ConstantCP)c).getNameAndTypeIndex(),
                                Constants.CONSTANT_NameAndType));
                break;
            default: // Never reached
                throw new RuntimeException("Unknown constant type "+tag);
        }
        return str;
    }

    public String constantToString(int index,byte tag)
            throws ClassFormatException{
        Constant c=getConstant(index,tag);
        return constantToString(c);
    }

    public void dump(DataOutputStream file) throws IOException{
        file.writeShort(constant_pool_count);
        for(int i=1;i<constant_pool_count;i++)
            if(constant_pool[i]!=null)
                constant_pool[i].dump(file);
    }

    public Constant[] getConstantPool(){
        return constant_pool;
    }

    public void setConstantPool(Constant[] constant_pool){
        this.constant_pool=constant_pool;
        constant_pool_count=(constant_pool==null)?0:constant_pool.length;
    }

    public String getConstantString(int index,byte tag)
            throws ClassFormatException{
        Constant c;
        int i;
        c=getConstant(index,tag);
        /** This switch() is not that elegant, since the two classes have the
         * same contents, they just differ in the name of the index
         * field variable.
         * But we want to stick to the JVM naming conventions closely though
         * we could have solved these more elegantly by using the same
         * variable name or by subclassing.
         */
        switch(tag){
            case Constants.CONSTANT_Class:
                i=((ConstantClass)c).getNameIndex();
                break;
            case Constants.CONSTANT_String:
                i=((ConstantString)c).getStringIndex();
                break;
            default:
                throw new RuntimeException("getConstantString called with illegal tag "+tag);
        }
        // Finally get the string from the constant pool
        c=getConstant(i,Constants.CONSTANT_Utf8);
        return ((ConstantUtf8)c).getBytes();
    }

    public Constant getConstant(int index,byte tag)
            throws ClassFormatException{
        Constant c;
        c=getConstant(index);
        if(c==null)
            throw new ClassFormatException("Constant pool at index "+index+" is null.");
        if(c.getTag()==tag)
            return c;
        else
            throw new ClassFormatException("Expected class `"+Constants.CONSTANT_NAMES[tag]+
                    "' at index "+index+" and got "+c);
    }

    public Constant getConstant(int index){
        if(index>=constant_pool.length||index<0)
            throw new ClassFormatException("Invalid constant pool reference: "+
                    index+". Constant pool size is: "+
                    constant_pool.length);
        return constant_pool[index];
    }

    public int getLength(){
        return constant_pool_count;
    }

    public void setConstant(int index,Constant constant){
        constant_pool[index]=constant;
    }

    public String toString(){
        StringBuffer buf=new StringBuffer();
        for(int i=1;i<constant_pool_count;i++)
            buf.append(i+")"+constant_pool[i]+"\n");
        return buf.toString();
    }

    public ConstantPool copy(){
        ConstantPool c=null;
        try{
            c=(ConstantPool)clone();
        }catch(CloneNotSupportedException e){
        }
        c.constant_pool=new Constant[constant_pool_count];
        for(int i=1;i<constant_pool_count;i++){
            if(constant_pool[i]!=null)
                c.constant_pool[i]=constant_pool[i].copy();
        }
        return c;
    }
}
