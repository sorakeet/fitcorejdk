/**
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.spi.http;

import java.util.Set;

public abstract class HttpContext{
    protected HttpHandler handler;

    public void setHandler(HttpHandler handler){
        this.handler=handler;
    }

    public abstract String getPath();

    public abstract Object getAttribute(String name);

    public abstract Set<String> getAttributeNames();
}
