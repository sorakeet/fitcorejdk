/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpStatusException;

public interface SnmpTableEntryFactory extends SnmpTableCallbackHandler{
    public void createNewEntry(SnmpMibSubRequest request,SnmpOid rowOid,
                               int depth,SnmpMibTable meta)
            throws SnmpStatusException;
}
