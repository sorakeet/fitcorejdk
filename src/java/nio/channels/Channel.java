/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels;

import java.io.Closeable;
import java.io.IOException;

public interface Channel extends Closeable{
    public boolean isOpen();

    public void close() throws IOException;
}
