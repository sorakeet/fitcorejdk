/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

public abstract class Instrument extends SoundbankResource{
    private final Patch patch;

    protected Instrument(Soundbank soundbank,Patch patch,String name,Class<?> dataClass){
        super(soundbank,name,dataClass);
        this.patch=patch;
    }

    public Patch getPatch(){
        return patch;
    }
}
