/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public interface InstructionTargeter{
    public boolean containsTarget(InstructionHandle ih);

    public void updateTarget(InstructionHandle old_ih,InstructionHandle new_ih);
}
