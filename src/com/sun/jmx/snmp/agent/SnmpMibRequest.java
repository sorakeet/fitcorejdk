/**
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;

import com.sun.jmx.snmp.SnmpEngine;
import com.sun.jmx.snmp.SnmpPdu;
import com.sun.jmx.snmp.SnmpVarBind;

import java.util.Enumeration;
import java.util.Vector;

public interface SnmpMibRequest{
    public Enumeration<SnmpVarBind> getElements();

    public Vector<SnmpVarBind> getSubList();

    public int getVersion();

    public int getRequestPduVersion();

    public SnmpEngine getEngine();

    public String getPrincipal();

    public int getSecurityLevel();

    public int getSecurityModel();

    public byte[] getContextName();

    public byte[] getAccessContextName();

    public Object getUserData();

    public int getVarIndex(SnmpVarBind varbind);

    public void addVarBind(SnmpVarBind varbind);

    public int getSize();

    public SnmpPdu getPdu();
}
