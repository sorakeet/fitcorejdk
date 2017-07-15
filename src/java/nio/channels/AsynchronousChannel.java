/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels;

import java.io.IOException;

public interface AsynchronousChannel
        extends Channel{
    @Override
    void close() throws IOException;
}
