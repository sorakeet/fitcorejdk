/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.mpm;

import com.sun.jmx.snmp.SnmpMsg;
import com.sun.jmx.snmp.SnmpSecurityParameters;

public interface SnmpMsgTranslator{
    public int getMsgId(SnmpMsg msg);

    public int getMsgMaxSize(SnmpMsg msg);

    public byte getMsgFlags(SnmpMsg msg);

    public int getMsgSecurityModel(SnmpMsg msg);

    public int getSecurityLevel(SnmpMsg msg);

    public byte[] getFlatSecurityParameters(SnmpMsg msg);

    public SnmpSecurityParameters getSecurityParameters(SnmpMsg msg);

    public byte[] getContextEngineId(SnmpMsg msg);

    public byte[] getContextName(SnmpMsg msg);

    public byte[] getRawContextName(SnmpMsg msg);

    public byte[] getAccessContext(SnmpMsg msg);

    public byte[] getEncryptedPdu(SnmpMsg msg);

    public void setContextName(SnmpMsg req,byte[] contextName);

    public void setContextEngineId(SnmpMsg req,byte[] contextEngineId);
}
