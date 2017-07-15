/**
 * Copyright (c) 1999, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

public class Patch{
    private final int bank;
    private final int program;

    public Patch(int bank,int program){
        this.bank=bank;
        this.program=program;
    }

    public int getBank(){
        return bank;
    }

    public int getProgram(){
        return program;
    }
}
