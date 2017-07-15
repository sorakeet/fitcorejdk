/**
 * Copyright (c) 1999, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled.spi;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract class AudioFileReader{
    public abstract AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException;

    public abstract AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException;

    public abstract AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException;

    public abstract AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException;

    public abstract AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException;

    public abstract AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException;
}
