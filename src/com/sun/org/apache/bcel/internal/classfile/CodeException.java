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

public final class CodeException
        implements Cloneable, Constants, Node, Serializable{
    private int start_pc;   // Range in the code the exception handler is
    private int end_pc;     // active. start_pc is inclusive, end_pc exclusive
    private int handler_pc;
    private int catch_type;

    public CodeException(CodeException c){
        this(c.getStartPC(),c.getEndPC(),c.getHandlerPC(),c.getCatchType());
    }

    public CodeException(int start_pc,int end_pc,int handler_pc,
                         int catch_type){
        this.start_pc=start_pc;
        this.end_pc=end_pc;
        this.handler_pc=handler_pc;
        this.catch_type=catch_type;
    }

    CodeException(DataInputStream file) throws IOException{
        this(file.readUnsignedShort(),file.readUnsignedShort(),
                file.readUnsignedShort(),file.readUnsignedShort());
    }

    public void accept(Visitor v){
        v.visitCodeException(this);
    }

    public final void dump(DataOutputStream file) throws IOException{
        file.writeShort(start_pc);
        file.writeShort(end_pc);
        file.writeShort(handler_pc);
        file.writeShort(catch_type);
    }

    public final int getCatchType(){
        return catch_type;
    }

    public final void setCatchType(int catch_type){
        this.catch_type=catch_type;
    }

    public final int getEndPC(){
        return end_pc;
    }

    public final void setEndPC(int end_pc){
        this.end_pc=end_pc;
    }

    public final int getHandlerPC(){
        return handler_pc;
    }

    public final void setHandlerPC(int handler_pc){
        this.handler_pc=handler_pc;
    }

    public final int getStartPC(){
        return start_pc;
    }

    public final void setStartPC(int start_pc){
        this.start_pc=start_pc;
    }

    public final String toString(){
        return "CodeException(start_pc = "+start_pc+
                ", end_pc = "+end_pc+
                ", handler_pc = "+handler_pc+", catch_type = "+catch_type+")";
    }

    public final String toString(ConstantPool cp){
        return toString(cp,true);
    }

    public final String toString(ConstantPool cp,boolean verbose){
        String str;
        if(catch_type==0)
            str="<Any exception>(0)";
        else
            str=Utility.compactClassName(cp.getConstantString(catch_type,CONSTANT_Class),false)+
                    (verbose?"("+catch_type+")":"");
        return start_pc+"\t"+end_pc+"\t"+handler_pc+"\t"+str;
    }

    public CodeException copy(){
        try{
            return (CodeException)clone();
        }catch(CloneNotSupportedException e){
        }
        return null;
    }
}
