/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;

import java.io.Serializable;

public abstract class SnmpPduPacket extends SnmpPdu implements Serializable{
    public byte[] community;
}
