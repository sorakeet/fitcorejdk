/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public abstract class CacheResponse{
    public abstract Map<String,List<String>> getHeaders() throws IOException;

    public abstract InputStream getBody() throws IOException;
}
