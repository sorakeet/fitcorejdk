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

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.classfile.Constant;
import com.sun.org.apache.bcel.internal.util.ByteSequence;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class InstructionList implements Serializable{
    private InstructionHandle start=null, end=null;
    private int length=0; // number of elements in list
    private int[] byte_positions; // byte code offsets corresponding to instructions
    private ArrayList observers;

    public InstructionList(){
    }

    public InstructionList(Instruction i){
        append(i);
    }

    public InstructionHandle append(Instruction i){
        InstructionHandle ih=InstructionHandle.getInstructionHandle(i);
        append(ih);
        return ih;
    }

    private void append(InstructionHandle ih){
        if(isEmpty()){
            start=end=ih;
            ih.next=ih.prev=null;
        }else{
            end.next=ih;
            ih.prev=end;
            ih.next=null;
            end=ih;
        }
        length++; // Update length
    }

    public boolean isEmpty(){
        return start==null;
    } // && end == null

    public InstructionList(BranchInstruction i){
        append(i);
    }

    public BranchHandle append(BranchInstruction i){
        BranchHandle ih=BranchHandle.getBranchHandle(i);
        append(ih);
        return ih;
    }

    public InstructionList(CompoundInstruction c){
        append(c.getInstructionList());
    }

    public InstructionHandle append(InstructionList il){
        if(il==null)
            throw new ClassGenException("Appending null InstructionList");
        if(il.isEmpty()) // Nothing to do
            return null;
        if(isEmpty()){
            start=il.start;
            end=il.end;
            length=il.length;
            il.clear();
            return start;
        }else
            return append(end,il);  // was end.instruction
    }

    public InstructionHandle append(InstructionHandle ih,InstructionList il){
        if(il==null)
            throw new ClassGenException("Appending null InstructionList");
        if(il.isEmpty()) // Nothing to do
            return ih;
        InstructionHandle next=ih.next, ret=il.start;
        ih.next=il.start;
        il.start.prev=ih;
        il.end.next=next;
        if(next!=null) // i == end ?
            next.prev=il.end;
        else
            end=il.end; // Update end ...
        length+=il.length; // Update length
        il.clear();
        return ret;
    }

    public InstructionList(byte[] code){
        ByteSequence bytes=new ByteSequence(code);
        InstructionHandle[] ihs=new InstructionHandle[code.length];
        int[] pos=new int[code.length]; // Can't be more than that
        int count=0; // Contains actual length
        /** Pass 1: Create an object for each byte code and append them
         * to the list.
         */
        try{
            while(bytes.available()>0){
                // Remember byte offset and associate it with the instruction
                int off=bytes.getIndex();
                pos[count]=off;
                /** Read one instruction from the byte stream, the byte position is set
                 * accordingly.
                 */
                Instruction i=Instruction.readInstruction(bytes);
                InstructionHandle ih;
                if(i instanceof BranchInstruction) // Use proper append() method
                    ih=append((BranchInstruction)i);
                else
                    ih=append(i);
                ih.setPosition(off);
                ihs[count]=ih;
                count++;
            }
        }catch(IOException e){
            throw new ClassGenException(e.toString());
        }
        byte_positions=new int[count]; // Trim to proper size
        System.arraycopy(pos,0,byte_positions,0,count);
        /** Pass 2: Look for BranchInstruction and update their targets, i.e.,
         * convert offsets to instruction handles.
         */
        for(int i=0;i<count;i++){
            if(ihs[i] instanceof BranchHandle){
                BranchInstruction bi=(BranchInstruction)ihs[i].instruction;
                int target=bi.position+bi.getIndex(); /** Byte code position:
                 * relative -> absolute. */
                // Search for target position
                InstructionHandle ih=findHandle(ihs,pos,count,target);
                if(ih==null) // Search failed
                    throw new ClassGenException("Couldn't find target for branch: "+bi);
                bi.setTarget(ih); // Update target
                // If it is a Select instruction, update all branch targets
                if(bi instanceof Select){ // Either LOOKUPSWITCH or TABLESWITCH
                    Select s=(Select)bi;
                    int[] indices=s.getIndices();
                    for(int j=0;j<indices.length;j++){
                        target=bi.position+indices[j];
                        ih=findHandle(ihs,pos,count,target);
                        if(ih==null) // Search failed
                            throw new ClassGenException("Couldn't find target for switch: "+bi);
                        s.setTarget(j,ih); // Update target
                    }
                }
            }
        }
    }

    public InstructionHandle findHandle(int pos){
        InstructionHandle[] ihs=getInstructionHandles();
        return findHandle(ihs,byte_positions,length,pos);
    }

    public static InstructionHandle findHandle(InstructionHandle[] ihs,
                                               int[] pos,int count,
                                               int target){
        int l=0, r=count-1;
        /** Do a binary search since the pos array is orderd.
         */
        do{
            int i=(l+r)/2;
            int j=pos[i];
            if(j==target) // target found
                return ihs[i];
            else if(target<j) // else constrain search area
                r=i-1;
            else // target > j
                l=i+1;
        }while(l<=r);
        return null;
    }

    public InstructionHandle[] getInstructionHandles(){
        InstructionHandle[] ihs=new InstructionHandle[length];
        InstructionHandle ih=start;
        for(int i=0;i<length;i++){
            ihs[i]=ih;
            ih=ih.next;
        }
        return ihs;
    }

    public InstructionHandle append(Instruction i,Instruction j){
        return append(i,new InstructionList(j));
    }

    public InstructionHandle append(Instruction i,InstructionList il){
        InstructionHandle ih;
        if((ih=findInstruction2(i))==null) // Also applies for empty list
            throw new ClassGenException("Instruction "+i+
                    " is not contained in this list.");
        return append(ih,il);
    }

    private InstructionHandle findInstruction2(Instruction i){
        for(InstructionHandle ih=end;ih!=null;ih=ih.prev)
            if(ih.instruction==i)
                return ih;
        return null;
    }

    public InstructionHandle append(Instruction i,CompoundInstruction c){
        return append(i,c.getInstructionList());
    }

    public InstructionHandle append(CompoundInstruction c){
        return append(c.getInstructionList());
    }

    public InstructionHandle append(InstructionHandle ih,CompoundInstruction c){
        return append(ih,c.getInstructionList());
    }

    public InstructionHandle append(InstructionHandle ih,Instruction i){
        return append(ih,new InstructionList(i));
    }

    public BranchHandle append(InstructionHandle ih,BranchInstruction i){
        BranchHandle bh=BranchHandle.getBranchHandle(i);
        InstructionList il=new InstructionList();
        il.append(bh);
        append(ih,il);
        return bh;
    }

    public InstructionHandle insert(Instruction i){
        InstructionHandle ih=InstructionHandle.getInstructionHandle(i);
        insert(ih);
        return ih;
    }

    private void insert(InstructionHandle ih){
        if(isEmpty()){
            start=end=ih;
            ih.next=ih.prev=null;
        }else{
            start.prev=ih;
            ih.next=start;
            ih.prev=null;
            start=ih;
        }
        length++;
    }

    public BranchHandle insert(BranchInstruction i){
        BranchHandle ih=BranchHandle.getBranchHandle(i);
        insert(ih);
        return ih;
    }

    public InstructionHandle insert(Instruction i,Instruction j){
        return insert(i,new InstructionList(j));
    }

    public InstructionHandle insert(Instruction i,InstructionList il){
        InstructionHandle ih;
        if((ih=findInstruction1(i))==null)
            throw new ClassGenException("Instruction "+i+
                    " is not contained in this list.");
        return insert(ih,il);
    }

    public InstructionHandle insert(InstructionHandle ih,InstructionList il){
        if(il==null)
            throw new ClassGenException("Inserting null InstructionList");
        if(il.isEmpty()) // Nothing to do
            return ih;
        InstructionHandle prev=ih.prev, ret=il.start;
        ih.prev=il.end;
        il.end.next=ih;
        il.start.prev=prev;
        if(prev!=null) // ih == start ?
            prev.next=il.start;
        else
            start=il.start; // Update start ...
        length+=il.length; // Update length
        il.clear();
        return ret;
    }

    private InstructionHandle findInstruction1(Instruction i){
        for(InstructionHandle ih=start;ih!=null;ih=ih.next)
            if(ih.instruction==i)
                return ih;
        return null;
    }

    public InstructionHandle insert(Instruction i,CompoundInstruction c){
        return insert(i,c.getInstructionList());
    }

    public InstructionHandle insert(CompoundInstruction c){
        return insert(c.getInstructionList());
    }

    public InstructionHandle insert(InstructionList il){
        if(isEmpty()){
            append(il); // Code is identical for this case
            return start;
        }else
            return insert(start,il);
    }

    public InstructionHandle insert(InstructionHandle ih,Instruction i){
        return insert(ih,new InstructionList(i));
    }

    public InstructionHandle insert(InstructionHandle ih,CompoundInstruction c){
        return insert(ih,c.getInstructionList());
    }

    public BranchHandle insert(InstructionHandle ih,BranchInstruction i){
        BranchHandle bh=BranchHandle.getBranchHandle(i);
        InstructionList il=new InstructionList();
        il.append(bh);
        insert(ih,il);
        return bh;
    }

    public void move(InstructionHandle ih,InstructionHandle target){
        move(ih,ih,target);
    }

    public void move(InstructionHandle start,InstructionHandle end,InstructionHandle target){
        // Step 1: Check constraints
        if((start==null)||(end==null))
            throw new ClassGenException("Invalid null handle: From "+start+" to "+end);
        if((target==start)||(target==end))
            throw new ClassGenException("Invalid range: From "+start+" to "+end+
                    " contains target "+target);
        for(InstructionHandle ih=start;ih!=end.next;ih=ih.next){
            if(ih==null) // At end of list, end not found yet
                throw new ClassGenException("Invalid range: From "+start+" to "+end);
            else if(ih==target) // target may be null
                throw new ClassGenException("Invalid range: From "+start+" to "+end+
                        " contains target "+target);
        }
        // Step 2: Temporarily remove the given instructions from the list
        InstructionHandle prev=start.prev, next=end.next;
        if(prev!=null)
            prev.next=next;
        else // start == this.start!
            this.start=next;
        if(next!=null)
            next.prev=prev;
        else // end == this.end!
            this.end=prev;
        start.prev=end.next=null;
        // Step 3: append after target
        if(target==null){ // append to start of list
            end.next=this.start;
            this.start=start;
        }else{
            next=target.next;
            target.next=start;
            start.prev=target;
            end.next=next;
            if(next!=null)
                next.prev=end;
        }
    }

    public void delete(Instruction i) throws TargetLostException{
        InstructionHandle ih;
        if((ih=findInstruction1(i))==null)
            throw new ClassGenException("Instruction "+i+
                    " is not contained in this list.");
        delete(ih);
    }

    public void delete(InstructionHandle ih) throws TargetLostException{
        remove(ih.prev,ih.next);
    }

    private void remove(InstructionHandle prev,InstructionHandle next)
            throws TargetLostException{
        InstructionHandle first, last; // First and last deleted instruction
        if((prev==null)&&(next==null)){ // singleton list
            first=last=start;
            start=end=null;
        }else{
            if(prev==null){ // At start of list
                first=start;
                start=next;
            }else{
                first=prev.next;
                prev.next=next;
            }
            if(next==null){ // At end of list
                last=end;
                end=prev;
            }else{
                last=next.prev;
                next.prev=prev;
            }
        }
        first.prev=null; // Completely separated from rest of list
        last.next=null;
        ArrayList target_vec=new ArrayList();
        for(InstructionHandle ih=first;ih!=null;ih=ih.next)
            ih.getInstruction().dispose(); // e.g. BranchInstructions release their targets
        StringBuffer buf=new StringBuffer("{ ");
        for(InstructionHandle ih=first;ih!=null;ih=next){
            next=ih.next;
            length--;
            if(ih.hasTargeters()){ // Still got targeters?
                target_vec.add(ih);
                buf.append(ih.toString(true)+" ");
                ih.next=ih.prev=null;
            }else
                ih.dispose();
        }
        buf.append("}");
        if(!target_vec.isEmpty()){
            InstructionHandle[] targeted=new InstructionHandle[target_vec.size()];
            target_vec.toArray(targeted);
            throw new TargetLostException(targeted,buf.toString());
        }
    }

    public void delete(Instruction from,Instruction to) throws TargetLostException{
        InstructionHandle from_ih, to_ih;
        if((from_ih=findInstruction1(from))==null)
            throw new ClassGenException("Instruction "+from+
                    " is not contained in this list.");
        if((to_ih=findInstruction2(to))==null)
            throw new ClassGenException("Instruction "+to+
                    " is not contained in this list.");
        delete(from_ih,to_ih);
    }

    public void delete(InstructionHandle from,InstructionHandle to)
            throws TargetLostException{
        remove(from.prev,to.next);
    }

    public boolean contains(InstructionHandle i){
        if(i==null)
            return false;
        for(InstructionHandle ih=start;ih!=null;ih=ih.next)
            if(ih==i)
                return true;
        return false;
    }

    public Instruction[] getInstructions(){
        ByteSequence bytes=new ByteSequence(getByteCode());
        ArrayList instructions=new ArrayList();
        try{
            while(bytes.available()>0){
                instructions.add(Instruction.readInstruction(bytes));
            }
        }catch(IOException e){
            throw new ClassGenException(e.toString());
        }
        Instruction[] result=new Instruction[instructions.size()];
        instructions.toArray(result);
        return result;
    }

    public byte[] getByteCode(){
        // Update position indices of instructions
        setPositions();
        ByteArrayOutputStream b=new ByteArrayOutputStream();
        DataOutputStream out=new DataOutputStream(b);
        try{
            for(InstructionHandle ih=start;ih!=null;ih=ih.next){
                Instruction i=ih.instruction;
                i.dump(out); // Traverse list
            }
        }catch(IOException e){
            System.err.println(e);
            return null;
        }
        return b.toByteArray();
    }

    public void setPositions(){
        setPositions(false);
    }

    public void setPositions(boolean check){
        int max_additional_bytes=0, additional_bytes=0;
        int index=0, count=0;
        int[] pos=new int[length];
        /** Pass 0: Sanity checks
         */
        if(check){
            for(InstructionHandle ih=start;ih!=null;ih=ih.next){
                Instruction i=ih.instruction;
                if(i instanceof BranchInstruction){ // target instruction within list?
                    Instruction inst=((BranchInstruction)i).getTarget().instruction;
                    if(!contains(inst))
                        throw new ClassGenException("Branch target of "+
                                Constants.OPCODE_NAMES[i.opcode]+":"+
                                inst+" not in instruction list");
                    if(i instanceof Select){
                        InstructionHandle[] targets=((Select)i).getTargets();
                        for(int j=0;j<targets.length;j++){
                            inst=targets[j].instruction;
                            if(!contains(inst))
                                throw new ClassGenException("Branch target of "+
                                        Constants.OPCODE_NAMES[i.opcode]+":"+
                                        inst+" not in instruction list");
                        }
                    }
                    if(!(ih instanceof BranchHandle))
                        throw new ClassGenException("Branch instruction "+
                                Constants.OPCODE_NAMES[i.opcode]+":"+
                                inst+" not contained in BranchHandle.");
                }
            }
        }
        /** Pass 1: Set position numbers and sum up the maximum number of bytes an
         * instruction may be shifted.
         */
        for(InstructionHandle ih=start;ih!=null;ih=ih.next){
            Instruction i=ih.instruction;
            ih.setPosition(index);
            pos[count++]=index;
            /** Get an estimate about how many additional bytes may be added, because
             * BranchInstructions may have variable length depending on the target
             * offset (short vs. int) or alignment issues (TABLESWITCH and
             * LOOKUPSWITCH).
             */
            switch(i.getOpcode()){
                case Constants.JSR:
                case Constants.GOTO:
                    max_additional_bytes+=2;
                    break;
                case Constants.TABLESWITCH:
                case Constants.LOOKUPSWITCH:
                    max_additional_bytes+=3;
                    break;
            }
            index+=i.getLength();
        }
        /** Pass 2: Expand the variable-length (Branch)Instructions depending on
         * the target offset (short or int) and ensure that branch targets are
         * within this list.
         */
        for(InstructionHandle ih=start;ih!=null;ih=ih.next)
            additional_bytes+=ih.updatePosition(additional_bytes,max_additional_bytes);
        /** Pass 3: Update position numbers (which may have changed due to the
         * preceding expansions), like pass 1.
         */
        index=count=0;
        for(InstructionHandle ih=start;ih!=null;ih=ih.next){
            Instruction i=ih.instruction;
            ih.setPosition(index);
            pos[count++]=index;
            index+=i.getLength();
        }
        byte_positions=new int[count]; // Trim to proper size
        System.arraycopy(pos,0,byte_positions,0,count);
    }

    public boolean contains(Instruction i){
        return findInstruction1(i)!=null;
    }

    public String toString(){
        return toString(true);
    }

    public String toString(boolean verbose){
        StringBuffer buf=new StringBuffer();
        for(InstructionHandle ih=start;ih!=null;ih=ih.next){
            buf.append(ih.toString(verbose)+"\n");
        }
        return buf.toString();
    }

    public Iterator iterator(){
        return new Iterator(){
            private InstructionHandle ih=start;

            public boolean hasNext(){
                return ih!=null;
            }

            public Object next(){
                InstructionHandle i=ih;
                ih=ih.next;
                return i;
            }

            public void remove(){
                throw new UnsupportedOperationException();
            }
        };
    }

    public int[] getInstructionPositions(){
        return byte_positions;
    }

    public InstructionList copy(){
        HashMap map=new HashMap();
        InstructionList il=new InstructionList();
        /** Pass 1: Make copies of all instructions, append them to the new list
         * and associate old instruction references with the new ones, i.e.,
         * a 1:1 mapping.
         */
        for(InstructionHandle ih=start;ih!=null;ih=ih.next){
            Instruction i=ih.instruction;
            Instruction c=i.copy(); // Use clone for shallow copy
            if(c instanceof BranchInstruction)
                map.put(ih,il.append((BranchInstruction)c));
            else
                map.put(ih,il.append(c));
        }
        /** Pass 2: Update branch targets.
         */
        InstructionHandle ih=start;
        InstructionHandle ch=il.start;
        while(ih!=null){
            Instruction i=ih.instruction;
            Instruction c=ch.instruction;
            if(i instanceof BranchInstruction){
                BranchInstruction bi=(BranchInstruction)i;
                BranchInstruction bc=(BranchInstruction)c;
                InstructionHandle itarget=bi.getTarget(); // old target
                // New target is in hash map
                bc.setTarget((InstructionHandle)map.get(itarget));
                if(bi instanceof Select){ // Either LOOKUPSWITCH or TABLESWITCH
                    InstructionHandle[] itargets=((Select)bi).getTargets();
                    InstructionHandle[] ctargets=((Select)bc).getTargets();
                    for(int j=0;j<itargets.length;j++){ // Update all targets
                        ctargets[j]=(InstructionHandle)map.get(itargets[j]);
                    }
                }
            }
            ih=ih.next;
            ch=ch.next;
        }
        return il;
    }

    public void replaceConstantPool(ConstantPoolGen old_cp,ConstantPoolGen new_cp){
        for(InstructionHandle ih=start;ih!=null;ih=ih.next){
            Instruction i=ih.instruction;
            if(i instanceof CPInstruction){
                CPInstruction ci=(CPInstruction)i;
                Constant c=old_cp.getConstant(ci.getIndex());
                ci.setIndex(new_cp.addConstant(c,old_cp));
            }
        }
    }

    public void dispose(){
        // Traverse in reverse order, because ih.next is overwritten
        for(InstructionHandle ih=end;ih!=null;ih=ih.prev)
        /** Causes BranchInstructions to release target and targeters, because it
         * calls dispose() on the contained instruction.
         */
            ih.dispose();
        clear();
    }

    private void clear(){
        start=end=null;
        length=0;
    }

    public InstructionHandle getStart(){
        return start;
    }

    public InstructionHandle getEnd(){
        return end;
    }

    public int getLength(){
        return length;
    }

    public int size(){
        return length;
    }

    public void redirectBranches(InstructionHandle old_target,
                                 InstructionHandle new_target){
        for(InstructionHandle ih=start;ih!=null;ih=ih.next){
            Instruction i=ih.getInstruction();
            if(i instanceof BranchInstruction){
                BranchInstruction b=(BranchInstruction)i;
                InstructionHandle target=b.getTarget();
                if(target==old_target)
                    b.setTarget(new_target);
                if(b instanceof Select){ // Either LOOKUPSWITCH or TABLESWITCH
                    InstructionHandle[] targets=((Select)b).getTargets();
                    for(int j=0;j<targets.length;j++) // Update targets
                        if(targets[j]==old_target)
                            ((Select)b).setTarget(j,new_target);
                }
            }
        }
    }

    public void redirectLocalVariables(LocalVariableGen[] lg,
                                       InstructionHandle old_target,
                                       InstructionHandle new_target){
        for(int i=0;i<lg.length;i++){
            InstructionHandle start=lg[i].getStart();
            InstructionHandle end=lg[i].getEnd();
            if(start==old_target)
                lg[i].setStart(new_target);
            if(end==old_target)
                lg[i].setEnd(new_target);
        }
    }

    public void redirectExceptionHandlers(CodeExceptionGen[] exceptions,
                                          InstructionHandle old_target,
                                          InstructionHandle new_target){
        for(int i=0;i<exceptions.length;i++){
            if(exceptions[i].getStartPC()==old_target)
                exceptions[i].setStartPC(new_target);
            if(exceptions[i].getEndPC()==old_target)
                exceptions[i].setEndPC(new_target);
            if(exceptions[i].getHandlerPC()==old_target)
                exceptions[i].setHandlerPC(new_target);
        }
    }

    public void addObserver(InstructionListObserver o){
        if(observers==null)
            observers=new ArrayList();
        observers.add(o);
    }

    public void removeObserver(InstructionListObserver o){
        if(observers!=null)
            observers.remove(o);
    }

    public void update(){
        if(observers!=null)
            for(Iterator e=observers.iterator();e.hasNext();)
                ((InstructionListObserver)e.next()).notify(this);
    }
}
