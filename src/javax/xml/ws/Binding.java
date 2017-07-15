/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

public interface Binding{
    public java.util.List<javax.xml.ws.handler.Handler> getHandlerChain();

    public void setHandlerChain(java.util.List<javax.xml.ws.handler.Handler> chain);

    String getBindingID();
}
