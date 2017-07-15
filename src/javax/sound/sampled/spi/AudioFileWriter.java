/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled.spi;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public abstract class AudioFileWriter{
    public boolean isFileTypeSupported(AudioFileFormat.Type fileType){
        AudioFileFormat.Type types[]=getAudioFileTypes();
        for(int i=0;i<types.length;i++){
            if(fileType.equals(types[i])){
                return true;
            }
        }
        return false;
    }

    public abstract AudioFileFormat.Type[] getAudioFileTypes();

    public boolean isFileTypeSupported(AudioFileFormat.Type fileType,AudioInputStream stream){
        AudioFileFormat.Type types[]=getAudioFileTypes(stream);
        for(int i=0;i<types.length;i++){
            if(fileType.equals(types[i])){
                return true;
            }
        }
        return false;
    }

    public abstract AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream stream);

    public abstract int write(AudioInputStream stream,AudioFileFormat.Type fileType,OutputStream out) throws IOException;

    public abstract int write(AudioInputStream stream,AudioFileFormat.Type fileType,File out) throws IOException;
}
