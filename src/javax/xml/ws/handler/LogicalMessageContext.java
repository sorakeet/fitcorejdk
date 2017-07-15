/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.handler;

import javax.xml.ws.LogicalMessage;

public interface LogicalMessageContext
        extends MessageContext{
    public LogicalMessage getMessage();
}
