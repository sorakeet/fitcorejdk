/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public final class BranchHandle extends InstructionHandle{
    private static BranchHandle bh_list=null; // List of reusable handles
    private BranchInstruction bi; // An alias in fact, but saves lots of casts

    private BranchHandle(BranchInstruction i){
        super(i);
        bi=i;
    }

    static final BranchHandle getBranchHandle(BranchInstruction i){
        if(bh_list==null)
            return new BranchHandle(i);
        else{
            BranchHandle bh=bh_list;
            bh_list=(BranchHandle)bh.next;
            bh.setInstruction(i);
            return bh;
        }
    }

    public void updateTarget(InstructionHandle old_ih,InstructionHandle new_ih){
        bi.updateTarget(old_ih,new_ih);
    }

    public InstructionHandle getTarget(){
        return bi.getTarget();
    }

    public void setTarget(InstructionHandle ih){
        bi.setTarget(ih);
    }

    public void setInstruction(Instruction i){
        super.setInstruction(i);
        if(!(i instanceof BranchInstruction))
            throw new ClassGenException("Assigning "+i+
                    " to branch handle which is not a branch instruction");
        bi=(BranchInstruction)i;
    }

    protected int updatePosition(int offset,int max_offset){
        int x=bi.updatePosition(offset,max_offset);
        i_position=bi.position;
        return x;
    }

    public int getPosition(){
        return bi.position;
    }

    void setPosition(int pos){
        i_position=bi.position=pos;
    }

    protected void addHandle(){
        next=bh_list;
        bh_list=this;
    }
}
