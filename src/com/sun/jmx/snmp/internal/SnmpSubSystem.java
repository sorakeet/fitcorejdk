/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.internal;

import com.sun.jmx.snmp.SnmpEngine;
import com.sun.jmx.snmp.SnmpUnknownModelException;

public interface SnmpSubSystem{
    public SnmpEngine getEngine();

    public void addModel(int id,SnmpModel model);

    public SnmpModel removeModel(int id) throws SnmpUnknownModelException;

    public SnmpModel getModel(int id) throws SnmpUnknownModelException;

    public int[] getModelIds();

    public String[] getModelNames();
}
