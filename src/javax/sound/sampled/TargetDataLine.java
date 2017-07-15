/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

public interface TargetDataLine extends DataLine{
    public void open(AudioFormat format,int bufferSize) throws LineUnavailableException;

    public void open(AudioFormat format) throws LineUnavailableException;

    public int read(byte[] b,int off,int len);
    /**
     * Obtains the number of sample frames of audio data that can be read from
     * the target data line without blocking.  Note that the return value
     * measures sample frames, not bytes.
     * @return the number of sample frames currently available for reading
     * @see SourceDataLine#availableWrite
     */
    //public int availableRead();
}
