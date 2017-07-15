/**
 * Copyright (c) 1999, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi;

public class MidiUnavailableException extends Exception{
    public MidiUnavailableException(){
        super();
    }

    public MidiUnavailableException(String message){
        super(message);
    }
}
