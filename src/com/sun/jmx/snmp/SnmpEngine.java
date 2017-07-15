/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public interface SnmpEngine{
    public int getEngineTime();

    public SnmpEngineId getEngineId();

    public int getEngineBoots();

    public SnmpUsmKeyHandler getUsmKeyHandler();
}
