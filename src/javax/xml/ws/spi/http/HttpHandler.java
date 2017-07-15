/**
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.spi.http;

import java.io.IOException;

public abstract class HttpHandler{
    public abstract void handle(HttpExchange exchange) throws IOException;
}
