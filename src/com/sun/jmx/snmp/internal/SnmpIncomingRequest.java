/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.internal;

import com.sun.jmx.snmp.*;

import java.net.InetAddress;

public interface SnmpIncomingRequest{
    public SnmpSecurityParameters getSecurityParameters();

    public boolean isReport();

    public boolean isResponse();

    public void noResponse();

    public String getPrincipal();

    public int getSecurityLevel();

    public int getSecurityModel();

    public byte[] getContextName();

    public byte[] getContextEngineId();

    public byte[] getAccessContext();

    public int encodeMessage(byte[] outputBytes)
            throws SnmpTooBigException;

    public void decodeMessage(byte[] inputBytes,
                              int byteCount,
                              InetAddress address,
                              int port)
            throws SnmpStatusException, SnmpUnknownSecModelException,
            SnmpBadSecurityLevelException;

    public SnmpMsg encodeSnmpPdu(SnmpPdu p,
                                 int maxDataLength)
            throws SnmpStatusException, SnmpTooBigException;

    public SnmpPdu decodeSnmpPdu()
            throws SnmpStatusException;

    public String printRequestMessage();

    public String printResponseMessage();
}
