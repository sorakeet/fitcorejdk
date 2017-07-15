/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public interface InstructionComparator{
    public static final InstructionComparator DEFAULT=
            new InstructionComparator(){
                public boolean equals(Instruction i1,Instruction i2){
                    if(i1.opcode==i2.opcode){
                        if(i1 instanceof Select){
                            InstructionHandle[] t1=((Select)i1).getTargets();
                            InstructionHandle[] t2=((Select)i2).getTargets();
                            if(t1.length==t2.length){
                                for(int i=0;i<t1.length;i++){
                                    if(t1[i]!=t2[i]){
                                        return false;
                                    }
                                }
                                return true;
                            }
                        }else if(i1 instanceof BranchInstruction){
                            return ((BranchInstruction)i1).target==
                                    ((BranchInstruction)i2).target;
                        }else if(i1 instanceof ConstantPushInstruction){
                            return ((ConstantPushInstruction)i1).getValue().
                                    equals(((ConstantPushInstruction)i2).getValue());
                        }else if(i1 instanceof IndexedInstruction){
                            return ((IndexedInstruction)i1).getIndex()==
                                    ((IndexedInstruction)i2).getIndex();
                        }else if(i1 instanceof NEWARRAY){
                            return ((NEWARRAY)i1).getTypecode()==((NEWARRAY)i2).getTypecode();
                        }else{
                            return true;
                        }
                    }
                    return false;
                }
            };

    public boolean equals(Instruction i1,Instruction i2);
}
