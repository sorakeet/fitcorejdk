/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.handler;

public interface HandlerResolver{
    public java.util.List<Handler> getHandlerChain(PortInfo portInfo);
}
