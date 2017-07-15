/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.internal;

import com.sun.jmx.snmp.SnmpSecurityException;
import com.sun.jmx.snmp.SnmpSecurityParameters;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.SnmpTooBigException;

public interface SnmpSecurityModel extends SnmpModel{
    public int generateRequestMsg(SnmpSecurityCache cache,
                                  int version,
                                  int msgID,
                                  int msgMaxSize,
                                  byte msgFlags,
                                  int msgSecurityModel,
                                  SnmpSecurityParameters params,
                                  byte[] contextEngineID,
                                  byte[] contextName,
                                  byte[] data,
                                  int dataLength,
                                  byte[] outputBytes)
            throws SnmpTooBigException, SnmpStatusException,
            SnmpSecurityException;

    public int generateResponseMsg(SnmpSecurityCache cache,
                                   int version,
                                   int msgID,
                                   int msgMaxSize,
                                   byte msgFlags,
                                   int msgSecurityModel,
                                   SnmpSecurityParameters params,
                                   byte[] contextEngineID,
                                   byte[] contextName,
                                   byte[] data,
                                   int dataLength,
                                   byte[] outputBytes)
            throws SnmpTooBigException, SnmpStatusException,
            SnmpSecurityException;

    public SnmpSecurityParameters
    processIncomingRequest(SnmpSecurityCache cache,
                           int version,
                           int msgID,
                           int msgMaxSize,
                           byte msgFlags,
                           int msgSecurityModel,
                           byte[] params,
                           byte[] contextEngineID,
                           byte[] contextName,
                           byte[] data,
                           byte[] encryptedPdu,
                           SnmpDecryptedPdu decryptedPdu)
            throws SnmpStatusException, SnmpSecurityException;

    public SnmpSecurityParameters processIncomingResponse(SnmpSecurityCache cache,
                                                          int version,
                                                          int msgID,
                                                          int msgMaxSize,
                                                          byte msgFlags,
                                                          int msgSecurityModel,
                                                          byte[] params,
                                                          byte[] contextEngineID,
                                                          byte[] contextName,
                                                          byte[] data,
                                                          byte[] encryptedPdu,
                                                          SnmpDecryptedPdu decryptedPdu)
            throws SnmpStatusException, SnmpSecurityException;

    public SnmpSecurityCache createSecurityCache();

    public void releaseSecurityCache(SnmpSecurityCache cache);
}
