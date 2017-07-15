/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.internal;

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpPdu;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.SnmpUnknownAccContrModelException;

public interface SnmpAccessControlSubSystem extends SnmpSubSystem{
    public void checkPduAccess(int version,
                               String principal,
                               int securityLevel,
                               int pduType,
                               int securityModel,
                               byte[] contextName,
                               SnmpPdu pdu) throws SnmpStatusException, SnmpUnknownAccContrModelException;

    public void checkAccess(int version,
                            String principal,
                            int securityLevel,
                            int pduType,
                            int securityModel,
                            byte[] contextName,
                            SnmpOid oid) throws SnmpStatusException, SnmpUnknownAccContrModelException;
}
