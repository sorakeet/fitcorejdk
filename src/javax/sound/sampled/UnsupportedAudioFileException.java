/**
 * Copyright (c) 1999, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public class UnsupportedAudioFileException extends Exception{
    public UnsupportedAudioFileException(){
        super();
    }

    public UnsupportedAudioFileException(String message){
        super(message);
    }
}
