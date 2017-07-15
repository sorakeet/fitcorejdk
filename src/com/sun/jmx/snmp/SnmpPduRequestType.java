/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public interface SnmpPduRequestType extends SnmpAckPdu{
    public int getErrorIndex();

    public void setErrorIndex(int i);

    public int getErrorStatus();

    public void setErrorStatus(int i);
}
