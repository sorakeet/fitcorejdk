/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpStatusException;

import javax.management.ObjectName;

public interface SnmpTableCallbackHandler{
    public void addEntryCb(int pos,SnmpOid row,ObjectName name,
                           Object entry,SnmpMibTable meta)
            throws SnmpStatusException;

    public void removeEntryCb(int pos,SnmpOid row,ObjectName name,
                              Object entry,SnmpMibTable meta)
            throws SnmpStatusException;
}
