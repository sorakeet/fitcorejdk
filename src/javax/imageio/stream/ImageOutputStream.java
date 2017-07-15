/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.stream;

import java.io.DataOutput;
import java.io.IOException;

public interface ImageOutputStream extends ImageInputStream, DataOutput{
    void write(int b) throws IOException;

    void write(byte b[]) throws IOException;

    void write(byte b[],int off,int len) throws IOException;

    void writeBoolean(boolean v) throws IOException;

    void writeByte(int v) throws IOException;

    void writeShort(int v) throws IOException;

    void writeChar(int v) throws IOException;

    void writeInt(int v) throws IOException;

    void writeLong(long v) throws IOException;

    void writeFloat(float v) throws IOException;

    void writeDouble(double v) throws IOException;

    void writeBytes(String s) throws IOException;

    void writeChars(String s) throws IOException;

    void writeUTF(String s) throws IOException;

    void writeShorts(short[] s,int off,int len) throws IOException;

    void writeChars(char[] c,int off,int len) throws IOException;

    void writeInts(int[] i,int off,int len) throws IOException;

    void writeLongs(long[] l,int off,int len) throws IOException;

    void writeFloats(float[] f,int off,int len) throws IOException;

    void writeDoubles(double[] d,int off,int len) throws IOException;

    void writeBit(int bit) throws IOException;

    void writeBits(long bits,int numBits) throws IOException;

    void flushBefore(long pos) throws IOException;
}
