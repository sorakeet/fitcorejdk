/**
 * Copyright (c) 1998, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public interface SnmpPduFactory{
    public SnmpPdu decodeSnmpPdu(SnmpMsg msg) throws SnmpStatusException;

    public SnmpMsg encodeSnmpPdu(SnmpPdu p,int maxDataLength)
            throws SnmpStatusException, SnmpTooBigException;
}
