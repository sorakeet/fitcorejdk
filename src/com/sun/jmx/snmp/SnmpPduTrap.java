/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpPduTrap extends SnmpPduPacket{
    private static final long serialVersionUID=-3670886636491433011L;
    public SnmpOid enterprise;
    public SnmpIpAddress agentAddr;
    public int genericTrap;
    public int specificTrap;
    public long timeStamp;

    public SnmpPduTrap(){
        type=pduV1TrapPdu;
        version=snmpVersionOne;
    }
}
