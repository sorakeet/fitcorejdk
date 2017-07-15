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

import com.sun.org.apache.bcel.internal.classfile.Utility;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class InstructionHandle implements java.io.Serializable{
    private static InstructionHandle ih_list=null; // List of reusable handles
    protected int i_position=-1; // byte code offset of instruction
    InstructionHandle next, prev;  // Will be set from the outside
    Instruction instruction;
    private HashSet targeters;
    private HashMap attributes;

    protected InstructionHandle(Instruction i){
        setInstruction(i);
    }

    static final InstructionHandle getInstructionHandle(Instruction i){
        if(ih_list==null)
            return new InstructionHandle(i);
        else{
            InstructionHandle ih=ih_list;
            ih_list=ih.next;
            ih.setInstruction(i);
            return ih;
        }
    }

    public final InstructionHandle getNext(){
        return next;
    }

    public final InstructionHandle getPrev(){
        return prev;
    }

    public final Instruction getInstruction(){
        return instruction;
    }

    public void setInstruction(Instruction i){ // Overridden in BranchHandle
        if(i==null)
            throw new ClassGenException("Assigning null to handle");
        if((this.getClass()!=BranchHandle.class)&&(i instanceof BranchInstruction))
            throw new ClassGenException("Assigning branch instruction "+i+" to plain handle");
        if(instruction!=null)
            instruction.dispose();
        instruction=i;
    }

    public Instruction swapInstruction(Instruction i){
        Instruction oldInstruction=instruction;
        instruction=i;
        return oldInstruction;
    }

    protected int updatePosition(int offset,int max_offset){
        i_position+=offset;
        return 0;
    }

    public int getPosition(){
        return i_position;
    }

    void setPosition(int pos){
        i_position=pos;
    }

    void dispose(){
        next=prev=null;
        instruction.dispose();
        instruction=null;
        i_position=-1;
        attributes=null;
        removeAllTargeters();
        addHandle();
    }

    protected void addHandle(){
        next=ih_list;
        ih_list=this;
    }

    public void removeAllTargeters(){
        if(targeters!=null)
            targeters.clear();
    }

    public void removeTargeter(InstructionTargeter t){
        targeters.remove(t);
    }

    public void addTargeter(InstructionTargeter t){
        if(targeters==null)
            targeters=new HashSet();
        //if(!targeters.contains(t))
        targeters.add(t);
    }

    public InstructionTargeter[] getTargeters(){
        if(!hasTargeters())
            return null;
        InstructionTargeter[] t=new InstructionTargeter[targeters.size()];
        targeters.toArray(t);
        return t;
    }

    public boolean hasTargeters(){
        return (targeters!=null)&&(targeters.size()>0);
    }

    public String toString(){
        return toString(true);
    }

    public String toString(boolean verbose){
        return Utility.format(i_position,4,false,' ')+": "+instruction.toString(verbose);
    }

    public void addAttribute(Object key,Object attr){
        if(attributes==null)
            attributes=new HashMap(3);
        attributes.put(key,attr);
    }

    public void removeAttribute(Object key){
        if(attributes!=null)
            attributes.remove(key);
    }

    public Object getAttribute(Object key){
        if(attributes!=null)
            return attributes.get(key);
        return null;
    }

    public Collection getAttributes(){
        return attributes.values();
    }

    public void accept(Visitor v){
        instruction.accept(v);
    }
}
