/**
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;

@Deprecated
public interface RemoteCall{
    @Deprecated
    ObjectOutput getOutputStream() throws IOException;

    @Deprecated
    void releaseOutputStream() throws IOException;

    @Deprecated
    ObjectInput getInputStream() throws IOException;

    @Deprecated
    void releaseInputStream() throws IOException;

    @Deprecated
    ObjectOutput getResultStream(boolean success) throws IOException,
            StreamCorruptedException;

    @Deprecated
    void executeCall() throws Exception;

    @Deprecated
    void done() throws IOException;
}
