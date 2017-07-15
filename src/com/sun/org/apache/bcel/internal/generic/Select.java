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

import com.sun.org.apache.bcel.internal.util.ByteSequence;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Select extends BranchInstruction
        implements VariableLengthInstruction, StackProducer{
    protected int[] match;        // matches, i.e., case 1: ...
    protected int[] indices;      // target offsets
    protected InstructionHandle[] targets;      // target objects in instruction list
    protected int fixed_length; // fixed length defined by subclasses
    protected int match_length; // number of cases
    protected int padding=0;  // number of pad bytes for alignment

    Select(){
    }

    Select(short opcode,int[] match,InstructionHandle[] targets,
           InstructionHandle target){
        super(opcode,target);
        this.targets=targets;
        for(int i=0;i<targets.length;i++){
            BranchInstruction.notifyTargetChanged(targets[i],this);
        }
        this.match=match;
        if((match_length=match.length)!=targets.length)
            throw new ClassGenException("Match and target array have not the same length");
        indices=new int[match_length];
    }

    @Override
    public void dump(DataOutputStream out) throws IOException{
        out.writeByte(opcode);
        for(int i=0;i<padding;i++) // Padding bytes
            out.writeByte(0);
        index=getTargetOffset();     // Write default target offset
        out.writeInt(index);
    }

    @Override
    protected int updatePosition(int offset,int max_offset){
        position+=offset; // Additional offset caused by preceding SWITCHs, GOTOs, etc.
        short old_length=length;
        /** Alignment on 4-byte-boundary, + 1, because of tag byte.
         */
        padding=(4-((position+1)%4))%4;
        length=(short)(fixed_length+padding); // Update length
        return length-old_length;
    }

    @Override
    public String toString(boolean verbose){
        final StringBuilder buf=new StringBuilder(super.toString(verbose));
        if(verbose){
            for(int i=0;i<match_length;i++){
                String s="null";
                if(targets[i]!=null)
                    s=targets[i].getInstruction().toString();
                buf.append("(").append(match[i]).append(", ")
                        .append(s).append(" = {").append(indices[i]).append("})");
            }
        }else
            buf.append(" ...");
        return buf.toString();
    }

    @Override
    protected void initFromFile(ByteSequence bytes,boolean wide) throws IOException{
        padding=(4-(bytes.getIndex()%4))%4; // Compute number of pad bytes
        for(int i=0;i<padding;i++){
            bytes.readByte();
        }
        // Default branch target common for both cases (TABLESWITCH, LOOKUPSWITCH)
        index=bytes.readInt();
    }

    @Override
    public void updateTarget(InstructionHandle old_ih,InstructionHandle new_ih){
        boolean targeted=false;
        if(target==old_ih){
            targeted=true;
            setTarget(new_ih);
        }
        for(int i=0;i<targets.length;i++){
            if(targets[i]==old_ih){
                targeted=true;
                setTarget(i,new_ih);
            }
        }
        if(!targeted)
            throw new ClassGenException("Not targeting "+old_ih);
    }

    public final void setTarget(int i,InstructionHandle target){
        notifyTargetChanging(targets[i],this);
        targets[i]=target;
        notifyTargetChanged(targets[i],this);
    }

    @Override
    public boolean containsTarget(InstructionHandle ih){
        if(target==ih)
            return true;
        for(int i=0;i<targets.length;i++)
            if(targets[i]==ih)
                return true;
        return false;
    }

    @Override
    void dispose(){
        super.dispose();
        for(int i=0;i<targets.length;i++)
            targets[i].removeTargeter(this);
    }

    public int[] getMatchs(){
        return match;
    }

    public int[] getIndices(){
        return indices;
    }

    public InstructionHandle[] getTargets(){
        return targets;
    }
}
