/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;

import com.sun.jmx.snmp.SnmpPdu;
import com.sun.jmx.snmp.SnmpStatusException;

public interface SnmpUserDataFactory{
    public Object allocateUserData(SnmpPdu requestPdu)
            throws SnmpStatusException;

    public void releaseUserData(Object userData,SnmpPdu responsePdu)
            throws SnmpStatusException;
}
