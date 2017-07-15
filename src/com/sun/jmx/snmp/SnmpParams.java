/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public abstract class SnmpParams implements SnmpDefinitions{
    private int protocolVersion=snmpVersionOne;

    SnmpParams(int version){
        protocolVersion=version;
    }

    SnmpParams(){
    }

    public abstract boolean allowSnmpSets();

    public int getProtocolVersion(){
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolversion){
        this.protocolVersion=protocolversion;
    }
}
