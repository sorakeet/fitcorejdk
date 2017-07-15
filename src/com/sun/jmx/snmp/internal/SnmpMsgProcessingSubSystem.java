/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.internal;

import com.sun.jmx.snmp.*;

public interface SnmpMsgProcessingSubSystem extends SnmpSubSystem{
    public SnmpSecuritySubSystem getSecuritySubSystem();

    public void setSecuritySubSystem(SnmpSecuritySubSystem security);

    public SnmpIncomingRequest getIncomingRequest(int model,
                                                  SnmpPduFactory factory)
            throws SnmpUnknownMsgProcModelException;

    public SnmpOutgoingRequest getOutgoingRequest(int model,
                                                  SnmpPduFactory factory) throws SnmpUnknownMsgProcModelException;

    public SnmpPdu getRequestPdu(int model,SnmpParams p,int type) throws SnmpUnknownMsgProcModelException, SnmpStatusException;

    public SnmpIncomingResponse getIncomingResponse(int model,
                                                    SnmpPduFactory factory) throws SnmpUnknownMsgProcModelException;

    public int encode(int version,
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
            throws SnmpTooBigException,
            SnmpUnknownMsgProcModelException;

    public int encodePriv(int version,
                          int msgID,
                          int msgMaxSize,
                          byte msgFlags,
                          int msgSecurityModel,
                          SnmpSecurityParameters params,
                          byte[] encryptedPdu,
                          byte[] outputBytes) throws SnmpTooBigException, SnmpUnknownMsgProcModelException;

    public SnmpDecryptedPdu decode(int version,
                                   byte[] pdu)
            throws SnmpStatusException, SnmpUnknownMsgProcModelException;

    public int encode(int version,
                      SnmpDecryptedPdu pdu,
                      byte[] outputBytes)
            throws SnmpTooBigException, SnmpUnknownMsgProcModelException;
}
