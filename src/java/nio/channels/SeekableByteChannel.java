/**
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface SeekableByteChannel
        extends ByteChannel{
    @Override
    int read(ByteBuffer dst) throws IOException;

    @Override
    int write(ByteBuffer src) throws IOException;

    long position() throws IOException;

    SeekableByteChannel position(long newPosition) throws IOException;

    long size() throws IOException;

    SeekableByteChannel truncate(long size) throws IOException;
}
