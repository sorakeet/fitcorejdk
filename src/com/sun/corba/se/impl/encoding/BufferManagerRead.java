/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.encoding;

import com.sun.corba.se.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

import java.nio.ByteBuffer;

public interface BufferManagerRead{
    public void processFragment(ByteBuffer byteBuffer,
                                FragmentMessage header);

    public ByteBufferWithInfo underflow(ByteBufferWithInfo bbwi);

    public void init(Message header);

    public MarkAndResetHandler getMarkAndResetHandler();

    public void cancelProcessing(int requestId);

    public void close(ByteBufferWithInfo bbwi);
}
