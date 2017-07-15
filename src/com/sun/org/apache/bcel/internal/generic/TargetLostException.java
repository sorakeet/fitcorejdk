/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public final class TargetLostException extends Exception{
    private InstructionHandle[] targets;

    TargetLostException(InstructionHandle[] t,String mesg){
        super(mesg);
        targets=t;
    }

    public InstructionHandle[] getTargets(){
        return targets;
    }
}
