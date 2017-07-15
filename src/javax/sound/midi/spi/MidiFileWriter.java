/**
 * Copyright (c) 1999, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi.spi;

import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public abstract class MidiFileWriter{
    public boolean isFileTypeSupported(int fileType){
        int types[]=getMidiFileTypes();
        for(int i=0;i<types.length;i++){
            if(fileType==types[i]){
                return true;
            }
        }
        return false;
    }

    public abstract int[] getMidiFileTypes();

    public boolean isFileTypeSupported(int fileType,Sequence sequence){
        int types[]=getMidiFileTypes(sequence);
        for(int i=0;i<types.length;i++){
            if(fileType==types[i]){
                return true;
            }
        }
        return false;
    }

    public abstract int[] getMidiFileTypes(Sequence sequence);

    public abstract int write(Sequence in,int fileType,OutputStream out)
            throws IOException;

    public abstract int write(Sequence in,int fileType,File out)
            throws IOException;
}
