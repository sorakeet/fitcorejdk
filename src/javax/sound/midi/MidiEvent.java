/**
 * Copyright (c) 1999, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

public class MidiEvent{
    // Instance variables
    private final MidiMessage message;
    private long tick;

    public MidiEvent(MidiMessage message,long tick){
        this.message=message;
        this.tick=tick;
    }

    public MidiMessage getMessage(){
        return message;
    }

    public long getTick(){
        return tick;
    }

    public void setTick(long tick){
        this.tick=tick;
    }
}
