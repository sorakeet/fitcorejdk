/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.internal;

import com.sun.jmx.snmp.*;
import com.sun.jmx.snmp.mpm.SnmpMsgTranslator;

public interface SnmpMsgProcessingModel extends SnmpModel{
    public SnmpOutgoingRequest getOutgoingRequest(SnmpPduFactory factory);

    public SnmpIncomingRequest getIncomingRequest(SnmpPduFactory factory);

    public SnmpIncomingResponse getIncomingResponse(SnmpPduFactory factory);

    public SnmpPdu getRequestPdu(SnmpParams p,int type) throws SnmpStatusException;

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
                      byte[] outputBytes) throws SnmpTooBigException;

    public int encodePriv(int version,
                          int msgID,
                          int msgMaxSize,
                          byte msgFlags,
                          int msgSecurityModel,
                          SnmpSecurityParameters params,
                          byte[] encryptedPdu,
                          byte[] outputBytes) throws SnmpTooBigException;

    public SnmpDecryptedPdu decode(byte[] pdu) throws SnmpStatusException;

    public int encode(SnmpDecryptedPdu pdu,
                      byte[] outputBytes) throws SnmpTooBigException;

    public SnmpMsgTranslator getMsgTranslator();

    public void setMsgTranslator(SnmpMsgTranslator translator);
}
