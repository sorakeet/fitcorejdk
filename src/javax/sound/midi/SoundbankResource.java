/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

public abstract class SoundbankResource{
    private final Soundbank soundBank;
    private final String name;
    private final Class dataClass;
    //private final int index;

    protected SoundbankResource(Soundbank soundBank,String name,Class<?> dataClass){
        this.soundBank=soundBank;
        this.name=name;
        this.dataClass=dataClass;
    }

    public Soundbank getSoundbank(){
        return soundBank;
    }

    public String getName(){
        return name;
    }

    public Class<?> getDataClass(){
        return dataClass;
    }

    public abstract Object getData();
    /**
     * Obtains the index of this <code>SoundbankResource</code> into the
     * <code>Soundbank's</code> set of <code>SoundbankResources</code>.
     * @return the wavetable index
     */
    //public int getIndex() {
    //  return index;
    //}
    /**
     * Obtains a list of the instruments in the sound bank that use the
     * <code>SoundbankResource</code> for sound synthesis.
     * @return an array of <code>Instruments</code> that reference this
     * <code>SoundbankResource</code>
     *
     * @see Instrument#getSamples
     */
    //public abstract Instrument[] getInstruments();
}
