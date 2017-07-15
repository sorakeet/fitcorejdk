/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.internal;

import com.sun.jmx.snmp.*;

import java.net.InetAddress;

public interface SnmpIncomingResponse{
    public InetAddress getAddress();

    public int getPort();

    public SnmpSecurityParameters getSecurityParameters();

    public void setSecurityCache(SnmpSecurityCache cache);

    public int getSecurityLevel();

    public int getSecurityModel();

    public byte[] getContextName();

    public SnmpMsg decodeMessage(byte[] inputBytes,
                                 int byteCount,
                                 InetAddress address,
                                 int port)
            throws SnmpStatusException, SnmpSecurityException;

    public SnmpPdu decodeSnmpPdu()
            throws SnmpStatusException;

    public int getRequestId(byte[] data) throws SnmpStatusException;

    public String printMessage();
}
