/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public interface SnmpEngineFactory{
    public SnmpEngine createEngine(SnmpEngineParameters p);

    public SnmpEngine createEngine(SnmpEngineParameters p,
                                   InetAddressAcl ipacl);
}
