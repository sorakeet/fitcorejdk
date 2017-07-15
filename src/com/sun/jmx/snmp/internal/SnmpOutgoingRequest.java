/**
 * Copyright (c) 2001, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.internal;

import com.sun.jmx.snmp.*;

public interface SnmpOutgoingRequest{
    public SnmpSecurityCache getSecurityCache();

    public int encodeMessage(byte[] outputBytes)
            throws SnmpStatusException,
            SnmpTooBigException, SnmpSecurityException,
            SnmpUnknownSecModelException, SnmpBadSecurityLevelException;

    public SnmpMsg encodeSnmpPdu(SnmpPdu p,
                                 int maxDataLength)
            throws SnmpStatusException, SnmpTooBigException;

    public String printMessage();
}
