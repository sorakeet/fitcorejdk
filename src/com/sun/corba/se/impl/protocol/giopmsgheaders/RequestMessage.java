/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.protocol.giopmsgheaders;

import com.sun.corba.se.spi.ior.ObjectKey;
import com.sun.corba.se.spi.servicecontext.ServiceContexts;
import org.omg.CORBA.Principal;

public interface RequestMessage extends Message{
    byte RESPONSE_EXPECTED_BIT=0x01;

    ServiceContexts getServiceContexts();

    int getRequestId();

    boolean isResponseExpected();

    byte[] getReserved();

    ObjectKey getObjectKey();

    String getOperation();

    Principal getPrincipal();

    // NOTE: This is a SUN PROPRIETARY EXTENSION
    void setThreadPoolToUse(int poolToUse);
} // interface RequestMessage
