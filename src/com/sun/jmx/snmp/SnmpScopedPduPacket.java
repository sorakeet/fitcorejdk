/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

import java.io.Serializable;

public abstract class SnmpScopedPduPacket extends SnmpPdu
        implements Serializable{
    public int msgMaxSize=0;
    public int msgId=0;
    public byte msgFlags=0;
    public int msgSecurityModel=0;
    public byte[] contextEngineId=null;
    public byte[] contextName=null;
    public SnmpSecurityParameters securityParameters=null;

    protected SnmpScopedPduPacket(){
        version=SnmpDefinitions.snmpVersionThree;
    }
}
