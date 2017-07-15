/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.handler;

public interface Handler<C extends MessageContext>{
    public boolean handleMessage(C context);

    public boolean handleFault(C context);

    public void close(MessageContext context);
}
