/**
 * Copyright (c) 1999, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.midi.spi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract class MidiFileReader{
    public abstract MidiFileFormat getMidiFileFormat(InputStream stream)
            throws InvalidMidiDataException, IOException;

    public abstract MidiFileFormat getMidiFileFormat(URL url)
            throws InvalidMidiDataException, IOException;

    public abstract MidiFileFormat getMidiFileFormat(File file)
            throws InvalidMidiDataException, IOException;

    public abstract Sequence getSequence(InputStream stream)
            throws InvalidMidiDataException, IOException;

    public abstract Sequence getSequence(URL url)
            throws InvalidMidiDataException, IOException;

    public abstract Sequence getSequence(File file)
            throws InvalidMidiDataException, IOException;
}
