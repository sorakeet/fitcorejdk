/**
 * Copyright (c) 1999, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi.spi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Soundbank;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract class SoundbankReader{
    public abstract Soundbank getSoundbank(URL url)
            throws InvalidMidiDataException, IOException;

    public abstract Soundbank getSoundbank(InputStream stream)
            throws InvalidMidiDataException, IOException;

    public abstract Soundbank getSoundbank(File file)
            throws InvalidMidiDataException, IOException;
}
