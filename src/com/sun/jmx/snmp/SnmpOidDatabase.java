/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;
// java import
//

import java.util.Vector;
// jmx import
//

public interface SnmpOidDatabase extends SnmpOidTable{
    public void add(SnmpOidTable table);

    public void remove(SnmpOidTable table) throws SnmpStatusException;

    public void removeAll();

    public SnmpOidRecord resolveVarName(String name) throws SnmpStatusException;

    public SnmpOidRecord resolveVarOid(String oid) throws SnmpStatusException;

    public Vector<?> getAllEntries();
    // We can't specify Vector<SnmpOidTable> because the subinterface SnmpOidTable
    // overrides this method to return Vector<SnmpOidRecord>
}
