/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.pept.protocol;

public interface ProtocolHandler{
    // REVISIT - return type
    public boolean handleRequest(MessageMediator messageMediator);
}
// End of file.
