/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public interface SnmpSecurityParameters{
    int encode(byte[] outputBytes) throws SnmpTooBigException;

    void decode(byte[] params) throws SnmpStatusException;

    String getPrincipal();
}
