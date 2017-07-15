/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import java.io.IOException;
import java.io.OutputStream;

public abstract class CacheRequest{
    public abstract OutputStream getBody() throws IOException;

    public abstract void abort();
}
