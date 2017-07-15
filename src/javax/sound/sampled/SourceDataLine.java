/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public interface SourceDataLine extends DataLine{
    public void open(AudioFormat format,int bufferSize) throws LineUnavailableException;

    public void open(AudioFormat format) throws LineUnavailableException;

    public int write(byte[] b,int off,int len);
    /**
     * Obtains the number of sample frames of audio data that can be written to
     * the mixer, via this data line, without blocking.  Note that the return
     * value measures sample frames, not bytes.
     * @return the number of sample frames currently available for writing
     * @see TargetDataLine#availableRead
     */
    //public int availableWrite();
}
