/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;
// java import
//

import java.util.Vector;

public interface SnmpOidTable{
    public SnmpOidRecord resolveVarName(String name)
            throws SnmpStatusException;

    public SnmpOidRecord resolveVarOid(String oid)
            throws SnmpStatusException;

    public Vector<?> getAllEntries();
}
