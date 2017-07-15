/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.stream;

import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteOrder;

public interface ImageInputStream extends DataInput, Closeable{
    ByteOrder getByteOrder();

    void setByteOrder(ByteOrder byteOrder);

    int read() throws IOException;

    int read(byte[] b) throws IOException;

    int read(byte[] b,int off,int len) throws IOException;

    void readBytes(IIOByteBuffer buf,int len) throws IOException;

    long readUnsignedInt() throws IOException;

    void readFully(byte[] b) throws IOException;

    void readFully(byte[] b,int off,int len) throws IOException;

    int skipBytes(int n) throws IOException;

    boolean readBoolean() throws IOException;

    byte readByte() throws IOException;

    int readUnsignedByte() throws IOException;

    short readShort() throws IOException;

    int readUnsignedShort() throws IOException;

    char readChar() throws IOException;

    int readInt() throws IOException;

    long readLong() throws IOException;

    float readFloat() throws IOException;

    double readDouble() throws IOException;

    String readLine() throws IOException;

    String readUTF() throws IOException;

    void readFully(short[] s,int off,int len) throws IOException;

    void readFully(char[] c,int off,int len) throws IOException;

    void readFully(int[] i,int off,int len) throws IOException;

    void readFully(long[] l,int off,int len) throws IOException;

    void readFully(float[] f,int off,int len) throws IOException;

    void readFully(double[] d,int off,int len) throws IOException;

    long getStreamPosition() throws IOException;

    int getBitOffset() throws IOException;

    void setBitOffset(int bitOffset) throws IOException;

    int readBit() throws IOException;

    long readBits(int numBits) throws IOException;

    long length() throws IOException;

    long skipBytes(long n) throws IOException;

    void seek(long pos) throws IOException;

    void mark();

    void reset() throws IOException;

    void flushBefore(long pos) throws IOException;

    void flush() throws IOException;

    long getFlushedPosition();

    boolean isCached();

    boolean isCachedMemory();

    boolean isCachedFile();

    void close() throws IOException;
}
